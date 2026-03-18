package backend.infrastructure.network;

import backend.domain.ports.AsterixDecoderPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JavaAsterixDecoderAdapter implements AsterixDecoderPort {

    private static final String DEFINITIONS_ROOT = "definitions/categories/";

    private final ObjectMapper objectMapper;
    private volatile JasterixCatalog catalog;

    public JavaAsterixDecoderAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode analyze(String inputFilePath, int previewLimit) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(inputFilePath));
            List<RawAsterixRecord> records = splitRecords(bytes);
            JasterixCatalog currentCatalog = getCatalog();
            int safePreviewLimit = Math.max(1, previewLimit);

            ObjectNode root = JsonNodeFactory.instance.objectNode();
            ObjectNode summary = root.putObject("summary");
            summary.put("all_records", records.size());
            summary.put("cat10_records", (int) records.stream().filter(record -> record.category == 10).count());
            summary.put("cat21_records", (int) records.stream().filter(record -> record.category == 21).count());
            summary.put("flights", 0);
            summary.put("export_limit", safePreviewLimit);

            ArrayNode categoriesNode = root.putArray("categories");
            Map<Integer, List<RawAsterixRecord>> grouped = records.stream()
                    .collect(Collectors.groupingBy(record -> record.category, LinkedHashMap::new, Collectors.toList()));

            grouped.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> categoriesNode.add(buildCategoryNode(entry.getKey(), entry.getValue(), currentCatalog, safePreviewLimit)));

            return root;
        } catch (IOException exception) {
            throw new IllegalStateException("ASTERIX-Datei konnte in Java nicht dekodiert werden.", exception);
        }
    }

    private ObjectNode buildCategoryNode(
            int category,
            List<RawAsterixRecord> records,
            JasterixCatalog currentCatalog,
            int previewLimit
    ) {
        String categoryKey = "%03d".formatted(category);
        JasterixCategoryMeta meta = currentCatalog.categories.get(categoryKey);
        JasterixRecordDefinition definition = currentCatalog.loadDefaultDefinition(categoryKey);

        ObjectNode categoryNode = JsonNodeFactory.instance.objectNode();
        categoryNode.put("category", category);
        categoryNode.put("category_key", categoryKey);
        categoryNode.put("count", records.size());
        categoryNode.put("description", meta == null || meta.comment == null ? "" : meta.comment);
        categoryNode.put("default_edition", meta == null || meta.defaultEdition == null ? "" : meta.defaultEdition);
        categoryNode.put("default_definition_file", meta == null || meta.defaultDefinitionFile == null ? "" : meta.defaultDefinitionFile);

        ArrayNode previews = categoryNode.putArray("previews");
        int max = Math.min(records.size(), previewLimit);
        for (int index = 0; index < max; index++) {
            previews.add(buildPreviewNode(index, records.get(index), definition));
        }

        return categoryNode;
    }

    private ObjectNode buildPreviewNode(int index, RawAsterixRecord record, JasterixRecordDefinition definition) {
        ObjectNode previewNode = JsonNodeFactory.instance.objectNode();
        previewNode.put("index", index);
        previewNode.put("length", record.length);
        previewNode.set("decoded", definition == null ? JsonNodeFactory.instance.objectNode() : decodeRecord(record, definition));
        return previewNode;
    }

    private ObjectNode decodeRecord(RawAsterixRecord record, JasterixRecordDefinition definition) {
        int[] offsetRef = new int[]{3};
        List<Integer> fspec = readFspec(record.data, offsetRef);
        List<String> presentItems = resolvePresentItems(fspec, definition.uapGroups);

        ObjectNode decoded = JsonNodeFactory.instance.objectNode();
        decoded.put("category", record.category);
        decoded.put("length", record.length);
        decoded.put("fspec", toHex(fspec));
        ArrayNode itemsNode = decoded.putArray("items");

        for (String itemId : presentItems) {
            ObjectNode itemNode = JsonNodeFactory.instance.objectNode();
            itemNode.put("id", itemId);

            JsonNode itemDefinition = definition.items.get(itemId);
            if (itemDefinition == null || !itemDefinition.isObject()) {
                itemNode.put("name", "");
                itemNode.put("comment", "");
                itemNode.put("consumed_bytes", 0);
                itemNode.put("note", "definition not found");
                itemNode.putNull("decoded");
                itemsNode.add(itemNode);
                continue;
            }

            ObjectNode itemObject = (ObjectNode) itemDefinition;
            itemNode.put("name", itemObject.path("name").asText(""));
            itemNode.put("comment", itemObject.path("comment").asText(""));
            int before = offsetRef[0];
            JsonNode value = decodeItem(itemObject, record.data, offsetRef);
            itemNode.put("consumed_bytes", Math.max(0, offsetRef[0] - before));
            itemNode.set("decoded", value == null ? JsonNodeFactory.instance.nullNode() : value);
            itemsNode.add(itemNode);
        }

        decoded.put("parsed_bytes", offsetRef[0]);
        decoded.put("remaining_bytes", Math.max(0, record.length - offsetRef[0]));
        return decoded;
    }

    private JsonNode decodeItem(ObjectNode itemDef, byte[] data, int[] offsetRef) {
        JsonNode dataFields = itemDef.get("data_fields");
        if (dataFields == null || !dataFields.isArray()) {
            return JsonNodeFactory.instance.nullNode();
        }

        ObjectNode result = JsonNodeFactory.instance.objectNode();
        Map<String, Integer> ctx = new HashMap<>();

        for (JsonNode fieldNode : dataFields) {
            if (!fieldNode.isObject()) {
                continue;
            }

            ObjectNode field = (ObjectNode) fieldNode;
            if (!shouldParseOptional(field, ctx)) {
                continue;
            }

            String type = field.path("type").asText("");
            String name = field.path("name").asText(type);
            JsonNode value = switch (type) {
                case "fixed_bytes" -> decodeFixedBytes(field, data, offsetRef);
                case "fixed_bitfield" -> decodeFixedBitfield(field, data, offsetRef, ctx);
                case "compound" -> decodeCompound(field, data, offsetRef);
                default -> decodeUnsupported(field, data, offsetRef);
            };
            result.set(name, value);
        }

        return result;
    }

    private boolean shouldParseOptional(ObjectNode field, Map<String, Integer> ctx) {
        boolean optional = field.path("optional").asBoolean(false);
        if (!optional) {
            return true;
        }

        String variableName = field.path("optional_variable_name").asText("");
        int requiredValue = field.path("optional_variable_value").asInt(1);
        Integer actualValue = ctx.get(variableName);
        return actualValue != null && actualValue == requiredValue;
    }

    private JsonNode decodeFixedBytes(ObjectNode field, byte[] data, int[] offsetRef) {
        int length = field.path("length").asInt(0);
        if (length <= 0 || offsetRef[0] + length > data.length) {
            offsetRef[0] = Math.min(data.length, offsetRef[0] + Math.max(0, length));
            return JsonNodeFactory.instance.nullNode();
        }

        long value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | Byte.toUnsignedInt(data[offsetRef[0] + i]);
        }
        offsetRef[0] += length;

        if (field.has("lsb") && field.get("lsb").isNumber()) {
            return JsonNodeFactory.instance.numberNode(value * field.get("lsb").asDouble());
        }

        return JsonNodeFactory.instance.numberNode(value);
    }

    private JsonNode decodeFixedBitfield(ObjectNode field, byte[] data, int[] offsetRef, Map<String, Integer> ctx) {
        int length = field.path("length").asInt(0);
        if (length <= 0 || offsetRef[0] + length > data.length) {
            offsetRef[0] = Math.min(data.length, offsetRef[0] + Math.max(0, length));
            return JsonNodeFactory.instance.nullNode();
        }

        byte[] raw = new byte[length];
        System.arraycopy(data, offsetRef[0], raw, 0, length);
        offsetRef[0] += length;

        ObjectNode values = JsonNodeFactory.instance.objectNode();
        JsonNode items = field.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode bitNode : items) {
                if (!bitNode.isObject()) {
                    continue;
                }

                ObjectNode bit = (ObjectNode) bitNode;
                if (!"fixed_bits".equals(bit.path("type").asText(""))) {
                    continue;
                }

                String bitName = bit.path("name").asText("bits");
                int start = bit.path("start_bit").asInt(0);
                int bitLength = bit.path("bit_length").asInt(1);
                long bitValue = extractBits(raw, start, bitLength);
                values.put(bitName, bitValue);
                ctx.put(bitName, (int) bitValue);
            }
        }

        if (values.isEmpty()) {
            values.put("raw_hex", bytesToHex(raw));
        }

        return values;
    }

    private JsonNode decodeCompound(ObjectNode field, byte[] data, int[] offsetRef) {
        List<Integer> fspec = readFspec(data, offsetRef);
        List<Boolean> presenceBits = expandPresenceBits(fspec);
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("fspec", toHex(fspec));

        JsonNode items = field.get("items");
        if (items == null || !items.isArray()) {
            return result;
        }

        for (JsonNode itemNode : items) {
            if (!itemNode.isObject()) {
                continue;
            }

            ObjectNode item = (ObjectNode) itemNode;
            int presenceIndex = item.path("optional_bitfield_index").asInt(-1);
            if (presenceIndex < 0 || presenceIndex >= presenceBits.size() || !presenceBits.get(presenceIndex)) {
                continue;
            }

            String name = item.path("name").asText("item_" + presenceIndex);
            JsonNode childFields = item.get("data_fields");
            if (childFields == null || !childFields.isArray()) {
                continue;
            }

            ObjectNode child = JsonNodeFactory.instance.objectNode();
            Map<String, Integer> ctx = new HashMap<>();
            for (JsonNode childFieldNode : childFields) {
                if (!childFieldNode.isObject()) {
                    continue;
                }

                ObjectNode childField = (ObjectNode) childFieldNode;
                String type = childField.path("type").asText("");
                String childName = childField.path("name").asText(type);
                JsonNode value = switch (type) {
                    case "fixed_bytes" -> decodeFixedBytes(childField, data, offsetRef);
                    case "fixed_bitfield" -> decodeFixedBitfield(childField, data, offsetRef, ctx);
                    case "compound" -> decodeCompound(childField, data, offsetRef);
                    default -> decodeUnsupported(childField, data, offsetRef);
                };
                child.set(childName, value);
            }

            result.set(name, child);
        }

        return result;
    }

    private JsonNode decodeUnsupported(ObjectNode field, byte[] data, int[] offsetRef) {
        int length = field.path("length").asInt(0);
        ObjectNode unsupported = JsonNodeFactory.instance.objectNode();
        unsupported.put("unsupported_type", field.path("type").asText(""));

        if (length <= 0 || offsetRef[0] + length > data.length) {
            unsupported.put("consumed_bytes", 0);
            return unsupported;
        }

        byte[] raw = new byte[length];
        System.arraycopy(data, offsetRef[0], raw, 0, length);
        offsetRef[0] += length;
        unsupported.put("raw_hex", bytesToHex(raw));
        return unsupported;
    }

    private List<Integer> readFspec(byte[] data, int[] offsetRef) {
        List<Integer> fspec = new ArrayList<>();
        while (offsetRef[0] < data.length) {
            int value = Byte.toUnsignedInt(data[offsetRef[0]++]);
            fspec.add(value);
            if ((value & 0x01) == 0) {
                break;
            }
        }
        return fspec;
    }

    private List<String> resolvePresentItems(List<Integer> fspec, List<List<String>> uapGroups) {
        List<String> items = new ArrayList<>();
        int octets = Math.min(fspec.size(), uapGroups.size());
        for (int octetIndex = 0; octetIndex < octets; octetIndex++) {
            int octet = fspec.get(octetIndex);
            List<String> group = uapGroups.get(octetIndex);
            for (int bit = 7, itemIndex = 0; bit >= 1 && itemIndex < group.size(); bit--, itemIndex++) {
                if (((octet >> bit) & 0x01) != 1) {
                    continue;
                }

                String item = group.get(itemIndex);
                if (!"-".equals(item) && !"RE".equals(item) && !"SP".equals(item)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private List<Boolean> expandPresenceBits(List<Integer> fspec) {
        List<Boolean> bits = new ArrayList<>();
        for (int octet : fspec) {
            for (int bit = 7; bit >= 1; bit--) {
                bits.add(((octet >> bit) & 0x01) == 1);
            }
        }
        return bits;
    }

    private long extractBits(byte[] raw, int startBit, int bitLength) {
        if (bitLength <= 0 || bitLength > 63) {
            return 0;
        }

        long value = 0;
        for (byte current : raw) {
            value = (value << 8) | Byte.toUnsignedInt(current);
        }

        long mask = (1L << bitLength) - 1L;
        return (value >> startBit) & mask;
    }

    private List<RawAsterixRecord> splitRecords(byte[] fileBytes) {
        List<RawAsterixRecord> records = new ArrayList<>();
        int offset = 0;
        while (offset + 3 <= fileBytes.length) {
            int category = Byte.toUnsignedInt(fileBytes[offset]);
            int length = (Byte.toUnsignedInt(fileBytes[offset + 1]) << 8) + Byte.toUnsignedInt(fileBytes[offset + 2]);
            if (length <= 0 || offset + length > fileBytes.length) {
                break;
            }

            byte[] record = new byte[length];
            System.arraycopy(fileBytes, offset, record, 0, length);
            records.add(new RawAsterixRecord(category, length, record));
            offset += length;
        }
        return records;
    }

    private JasterixCatalog getCatalog() throws IOException {
        if (catalog == null) {
            synchronized (this) {
                if (catalog == null) {
                    catalog = JasterixCatalog.load(objectMapper);
                }
            }
        }
        return catalog;
    }

    private String toHex(List<Integer> values) {
        StringBuilder builder = new StringBuilder();
        for (int value : values) {
            builder.append("%02X".formatted(value));
        }
        return builder.toString();
    }

    private String bytesToHex(byte[] values) {
        StringBuilder builder = new StringBuilder();
        for (byte value : values) {
            builder.append("%02X".formatted(Byte.toUnsignedInt(value)));
        }
        return builder.toString();
    }

    private static final class JasterixCatalog {
        private final Map<String, JasterixCategoryMeta> categories;
        private final Map<String, JasterixRecordDefinition> definitions = new HashMap<>();
        private final ObjectMapper objectMapper;

        private JasterixCatalog(Map<String, JasterixCategoryMeta> categories, ObjectMapper objectMapper) {
            this.categories = categories;
            this.objectMapper = objectMapper;
        }

        private static JasterixCatalog load(ObjectMapper objectMapper) throws IOException {
            try (InputStream inputStream = new ClassPathResource(DEFINITIONS_ROOT + "categories.json").getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                Map<String, JasterixCategoryMeta> categories = new LinkedHashMap<>();
                root.fieldNames().forEachRemaining(categoryKey -> {
                    JsonNode node = root.path(categoryKey);
                    String defaultEdition = node.path("default_edition").asText("");
                    String defaultDefinitionFile = "";
                    JsonNode editions = node.path("editions");
                    if (editions.has(defaultEdition)) {
                        defaultDefinitionFile = editions.path(defaultEdition).path("file").asText("");
                    }

                    categories.put(categoryKey, new JasterixCategoryMeta(
                            categoryKey,
                            node.path("comment").asText(""),
                            defaultEdition,
                            defaultDefinitionFile
                    ));
                });
                return new JasterixCatalog(categories, objectMapper);
            }
        }

        private JasterixRecordDefinition loadDefaultDefinition(String categoryKey) {
            if (definitions.containsKey(categoryKey)) {
                return definitions.get(categoryKey);
            }

            JasterixCategoryMeta meta = categories.get(categoryKey);
            if (meta == null || meta.defaultDefinitionFile == null || meta.defaultDefinitionFile.isBlank()) {
                definitions.put(categoryKey, null);
                return null;
            }

            try (InputStream inputStream = new ClassPathResource(DEFINITIONS_ROOT + meta.defaultDefinitionFile).getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                List<List<String>> uapGroups = new ArrayList<>();
                List<String> currentGroup = new ArrayList<>();
                JsonNode uap = root.path("uap");
                if (uap.isArray()) {
                    for (JsonNode entry : uap) {
                        String token = entry.asText("");
                        if ("FX".equals(token)) {
                            uapGroups.add(currentGroup);
                            currentGroup = new ArrayList<>();
                        } else {
                            currentGroup.add(token);
                        }
                    }
                    if (!currentGroup.isEmpty()) {
                        uapGroups.add(currentGroup);
                    }
                }

                Map<String, JsonNode> items = new LinkedHashMap<>();
                JsonNode itemArray = root.path("items");
                if (itemArray.isArray()) {
                    for (JsonNode item : itemArray) {
                        String number = item.path("number").asText("");
                        if (!number.isBlank()) {
                            items.put(number, item);
                        }
                    }
                }

                JasterixRecordDefinition definition = new JasterixRecordDefinition(categoryKey, uapGroups, items);
                definitions.put(categoryKey, definition);
                return definition;
            } catch (IOException exception) {
                definitions.put(categoryKey, null);
                return null;
            }
        }
    }

    private record JasterixCategoryMeta(
            String key,
            String comment,
            String defaultEdition,
            String defaultDefinitionFile
    ) {
    }

    private record JasterixRecordDefinition(
            String categoryKey,
            List<List<String>> uapGroups,
            Map<String, JsonNode> items
    ) {
    }

    private record RawAsterixRecord(int category, int length, byte[] data) {
    }
}
