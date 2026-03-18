package backend.application.usecase;

import backend.domain.model.AsterixAnalysisResult;
import backend.domain.model.AsterixAnalysisVisualization;
import backend.domain.model.AsterixCategoryAnalysis;
import backend.domain.model.AsterixCategoryDistributionItem;
import backend.domain.model.AsterixCategoryPage;
import backend.domain.model.AsterixFspecOctet;
import backend.domain.model.AsterixRecordAnalysis;
import backend.domain.model.AsterixSummary;
import backend.domain.model.AsterixTimelineCategoryCount;
import backend.domain.model.AsterixTrafficTimelineBucket;
import backend.domain.model.AsterixUapItem;
import backend.domain.ports.AsterixDecoderPort;
import backend.infrastructure.storage.AsterixAnalysisSessionStore;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyzeAsterixFileUseCase {

    private final AsterixDecoderPort decoderPort;
    private final int previewLimit;
    private final AsterixAnalysisSessionStore sessionStore;

    public AnalyzeAsterixFileUseCase(
            AsterixDecoderPort decoderPort,
            int previewLimit,
            AsterixAnalysisSessionStore sessionStore
    ) {
        this.decoderPort = decoderPort;
        this.previewLimit = previewLimit;
        this.sessionStore = sessionStore;
    }

    public AsterixAnalysisResult execute(String analysisId, String originalFileName, byte[] content, int pageSize) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Bitte eine ASTERIX-Datei auswaehlen.");
        }

        Path inputFile = null;
        try {
            inputFile = Files.createTempFile("asterix-upload-", sanitizeFileName(originalFileName));
            Files.write(inputFile, content);

            JsonNode cliResult = decoderPort.analyze(inputFile.toString(), pageSize);
            List<RawAsterixRecord> rawRecords = splitRecords(content);
            Map<String, List<RawAsterixRecord>> recordsByCategory = rawRecords.stream()
                    .collect(Collectors.groupingBy(
                            record -> "%03d".formatted(record.category()),
                            LinkedHashMap::new,
                            Collectors.toList()));

            List<AsterixCategoryAnalysis> categories = buildCategories(cliResult.path("categories"), recordsByCategory, 1, pageSize);
            JsonNode summaryNode = cliResult.path("summary");

            AsterixSummary summary = new AsterixSummary(
                    rawRecords.size(),
                    categories.size(),
                    summaryNode.path("cat10_records").asInt(0),
                    summaryNode.path("cat21_records").asInt(0),
                    summaryNode.path("flights").asInt(0),
                    previewLimit
            );
            AsterixAnalysisVisualization visualization = buildVisualization(rawRecords, categories);

            return new AsterixAnalysisResult(
                    analysisId,
                    safeText(originalFileName, "upload.ast"),
                    content.length,
                    Instant.now().toString(),
                    summary,
                    categories,
                    visualization
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Upload-Datei konnte nicht verarbeitet werden.", exception);
        } finally {
            if (inputFile != null) {
                try {
                    Files.deleteIfExists(inputFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public AsterixAnalysisResult execute(Path inputFile, String originalFileName, long fileSizeBytes) {
        return execute("transient", inputFile, originalFileName, fileSizeBytes, previewLimit);
    }

    public AsterixAnalysisResult execute(
            String analysisId,
            Path inputFile,
            String originalFileName,
            long fileSizeBytes,
            int pageSize
    ) {
        if (inputFile == null || !Files.exists(inputFile)) {
            throw new IllegalArgumentException("Die Upload-Datei wurde nicht gefunden.");
        }

        if (fileSizeBytes <= 0) {
            throw new IllegalArgumentException("Bitte eine ASTERIX-Datei auswaehlen.");
        }

        try {
            JsonNode cliResult = decoderPort.analyze(inputFile.toString(), pageSize);
            byte[] content = Files.readAllBytes(inputFile);
            List<RawAsterixRecord> rawRecords = splitRecords(content);
            Map<String, List<RawAsterixRecord>> recordsByCategory = rawRecords.stream()
                    .collect(Collectors.groupingBy(
                            record -> "%03d".formatted(record.category()),
                            LinkedHashMap::new,
                            Collectors.toList()));

            List<AsterixCategoryAnalysis> categories = buildCategories(cliResult.path("categories"), recordsByCategory, 1, pageSize);
            JsonNode summaryNode = cliResult.path("summary");

            AsterixSummary summary = new AsterixSummary(
                    rawRecords.size(),
                    categories.size(),
                    summaryNode.path("cat10_records").asInt(0),
                    summaryNode.path("cat21_records").asInt(0),
                    summaryNode.path("flights").asInt(0),
                    previewLimit
            );
            AsterixAnalysisVisualization visualization = buildVisualization(rawRecords, categories);

            return new AsterixAnalysisResult(
                    analysisId,
                    safeText(originalFileName, "upload.ast"),
                    fileSizeBytes,
                    Instant.now().toString(),
                    summary,
                    categories,
                    visualization
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Upload-Datei konnte nicht verarbeitet werden.", exception);
        }
    }

    public AsterixCategoryPage loadCategoryPage(Path inputFile, String categoryKey, int page, int pageSize) {
        return loadCategoryPage("transient", inputFile, categoryKey, page, pageSize);
    }

    public AsterixCategoryPage loadCategoryPage(
            String analysisId,
            Path inputFile,
            String categoryKey,
            int page,
            int pageSize
    ) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, pageSize);
        AsterixCategoryPage cachedPage = sessionStore.getCategoryPage(analysisId, categoryKey, safePage, safePageSize);
        if (cachedPage != null) {
            return cachedPage;
        }

        try {
            int requestedLimit = safePage * safePageSize;
            JsonNode cliResult = decoderPort.analyze(inputFile.toString(), requestedLimit);
            byte[] content = Files.readAllBytes(inputFile);
            Map<String, List<RawAsterixRecord>> recordsByCategory = splitRecords(content).stream()
                    .collect(Collectors.groupingBy(
                            record -> "%03d".formatted(record.category()),
                            LinkedHashMap::new,
                            Collectors.toList()));

            List<RawAsterixRecord> rawRecords = recordsByCategory.getOrDefault(categoryKey, List.of());
            int totalRecords = rawRecords.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / safePageSize));
            int startIndex = Math.min((safePage - 1) * safePageSize, totalRecords);

            JsonNode categoryNode = findCategoryNode(cliResult.path("categories"), categoryKey);
            List<AsterixRecordAnalysis> records = buildRecords(
                    categoryNode == null ? null : categoryNode.path("previews"),
                    rawRecords,
                    startIndex,
                    safePageSize
            );

            AsterixCategoryPage categoryPage = new AsterixCategoryPage(
                    categoryKey,
                    Math.min(safePage, totalPages),
                    safePageSize,
                    totalPages,
                    totalRecords,
                    records
            );
            sessionStore.putCategoryPage(analysisId, categoryKey, categoryPage.currentPage(), safePageSize, categoryPage);
            return categoryPage;
        } catch (IOException exception) {
            throw new IllegalStateException("Kategorie-Seite konnte nicht geladen werden.", exception);
        }
    }

    private List<AsterixCategoryAnalysis> buildCategories(
            JsonNode categoriesNode,
            Map<String, List<RawAsterixRecord>> recordsByCategory,
            int page,
            int pageSize
    ) {
        List<AsterixCategoryAnalysis> result = new ArrayList<>();
        if (!categoriesNode.isArray()) {
            return result;
        }

        for (JsonNode categoryNode : categoriesNode) {
            String categoryKey = categoryNode.path("category_key").asText();
            List<RawAsterixRecord> rawCategoryRecords = recordsByCategory.getOrDefault(categoryKey, List.of());
            List<AsterixRecordAnalysis> records = buildRecords(categoryNode.path("previews"), rawCategoryRecords, 0, pageSize);
            String uapItems = collectCategoryItemIds(records);
            int totalPages = Math.max(1, (int) Math.ceil((double) rawCategoryRecords.size() / Math.max(1, pageSize)));

            result.add(new AsterixCategoryAnalysis(
                    categoryNode.path("category").asInt(),
                    categoryKey,
                    categoryNode.path("count").asInt(rawCategoryRecords.size()),
                    safeText(categoryNode.path("description").asText(null), "(no description)"),
                    safeText(categoryNode.path("default_edition").asText(null), "-"),
                    extractFileName(categoryNode.path("default_definition_file").asText(null)),
                    uapItems,
                    page,
                    pageSize,
                    totalPages,
                    records
            ));
        }

        return result;
    }

    private AsterixAnalysisVisualization buildVisualization(
            List<RawAsterixRecord> rawRecords,
            List<AsterixCategoryAnalysis> categories
    ) {
        return new AsterixAnalysisVisualization(
                buildCategoryDistribution(categories, rawRecords.size()),
                buildTrafficTimeline(rawRecords)
        );
    }

    private List<AsterixCategoryDistributionItem> buildCategoryDistribution(
            List<AsterixCategoryAnalysis> categories,
            int totalRecords
    ) {
        if (totalRecords <= 0) {
            return List.of();
        }

        return categories.stream()
                .sorted(Comparator.comparingInt(AsterixCategoryAnalysis::count).reversed())
                .map(category -> new AsterixCategoryDistributionItem(
                        category.categoryKey(),
                        "CAT" + category.categoryKey(),
                        category.count(),
                        (category.count() * 100.0) / totalRecords
                ))
                .toList();
    }

    private List<AsterixTrafficTimelineBucket> buildTrafficTimeline(List<RawAsterixRecord> rawRecords) {
        if (rawRecords.isEmpty()) {
            return List.of();
        }

        int bucketCount = Math.min(32, Math.max(8, (int) Math.ceil(rawRecords.size() / 5000.0)));
        List<AsterixTrafficTimelineBucket> buckets = new ArrayList<>(bucketCount);

        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            int startInclusive = (int) Math.floor((double) bucketIndex * rawRecords.size() / bucketCount);
            int endExclusive = (int) Math.floor((double) (bucketIndex + 1) * rawRecords.size() / bucketCount);
            if (endExclusive <= startInclusive) {
                continue;
            }

            Map<String, Integer> categoryCounts = new LinkedHashMap<>();
            for (int recordIndex = startInclusive; recordIndex < endExclusive; recordIndex++) {
                String categoryKey = "%03d".formatted(rawRecords.get(recordIndex).category());
                categoryCounts.merge(categoryKey, 1, Integer::sum);
            }

            List<AsterixTimelineCategoryCount> bucketCategories = categoryCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .map(entry -> new AsterixTimelineCategoryCount(entry.getKey(), entry.getValue()))
                    .toList();

            buckets.add(new AsterixTrafficTimelineBucket(
                    bucketIndex + 1,
                    startInclusive,
                    endExclusive - 1,
                    endExclusive - startInclusive,
                    bucketCategories
            ));
        }

        return buckets;
    }

    private List<AsterixRecordAnalysis> buildRecords(JsonNode previewsNode, List<RawAsterixRecord> rawRecords, int startIndex, int pageSize) {
        List<AsterixRecordAnalysis> records = new ArrayList<>();
        if (previewsNode == null || !previewsNode.isArray()) {
            return records;
        }

        int endExclusive = Math.min(startIndex + pageSize, Math.min(previewsNode.size(), rawRecords.size()));
        for (int index = startIndex; index < endExclusive; index++) {
            JsonNode previewNode = previewsNode.get(index);
            RawAsterixRecord rawRecord = rawRecords.get(index);
            JsonNode decoded = previewNode.path("decoded");

            List<AsterixUapItem> uapItems = extractUapItems(decoded.path("items"));
            records.add(new AsterixRecordAnalysis(
                    previewNode.path("index").asInt(index),
                    previewNode.path("length").asInt(rawRecord.length()),
                    safeText(decoded.path("fspec").asText(null), "n/a"),
                    decoded.path("remaining_bytes").asInt(0),
                    collectRecordItemIds(uapItems),
                    toUnsignedByteList(rawRecord.data()),
                    buildFspecOctets(rawRecord.data()),
                    uapItems
            ));
        }

        return records;
    }

    private JsonNode findCategoryNode(JsonNode categoriesNode, String categoryKey) {
        if (!categoriesNode.isArray()) {
            return null;
        }

        for (JsonNode categoryNode : categoriesNode) {
            if (categoryKey.equals(categoryNode.path("category_key").asText())) {
                return categoryNode;
            }
        }

        return null;
    }

    private List<AsterixUapItem> extractUapItems(JsonNode itemsNode) {
        List<AsterixUapItem> items = new ArrayList<>();
        if (!itemsNode.isArray()) {
            return items;
        }

        for (JsonNode itemNode : itemsNode) {
            String valuePreview = formatValue(itemNode.get("decoded"));
            int consumedBytes = itemNode.path("consumed_bytes").asInt(0);
            items.add(new AsterixUapItem(
                    safeText(itemNode.path("id").asText(null), "-"),
                    safeText(itemNode.path("name").asText(null), "-"),
                    safeText(itemNode.path("comment").asText(null), "-"),
                    valuePreview,
                    consumedBytes,
                    classifyStatus(itemNode, valuePreview, consumedBytes)
            ));
        }

        return items;
    }

    private String classifyStatus(JsonNode itemNode, String valuePreview, int consumedBytes) {
        String note = itemNode.path("note").asText("");
        if (note.toLowerCase().contains("definition not found")) {
            return "MISSING_DEF";
        }

        if (note.toLowerCase().contains("error")) {
            return "ERROR";
        }

        if (valuePreview.toLowerCase().contains("unsupported_type")) {
            return "LIMITED";
        }

        if ("-".equals(valuePreview) || "{}".equals(valuePreview) || "[]".equals(valuePreview)) {
            return "EMPTY";
        }

        if (consumedBytes <= 0) {
            return "PARTIAL";
        }

        return "OK";
    }

    private String formatValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "-";
        }

        if (node.isTextual()) {
            return node.asText();
        }

        return node.toString();
    }

    private String collectCategoryItemIds(List<AsterixRecordAnalysis> records) {
        return records.stream()
                .flatMap(record -> record.uapItems().stream())
                .map(AsterixUapItem::id)
                .filter(id -> !id.isBlank() && !"-".equals(id))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", ", "", ""));
    }

    private String collectRecordItemIds(List<AsterixUapItem> items) {
        String joined = items.stream()
                .map(AsterixUapItem::id)
                .filter(id -> !id.isBlank() && !"-".equals(id))
                .collect(Collectors.joining(","));
        return joined.isBlank() ? "-" : joined;
    }

    private List<AsterixFspecOctet> buildFspecOctets(byte[] bytes) {
        List<AsterixFspecOctet> octets = new ArrayList<>();
        int offset = 3;
        int octetIndex = 1;

        while (offset < bytes.length) {
            int value = Byte.toUnsignedInt(bytes[offset++]);
            List<String> setBits = new ArrayList<>();
            for (int bit = 7; bit >= 1; bit--) {
                if (((value >> bit) & 1) == 1) {
                    setBits.add("b" + (bit + 1));
                }
            }

            int fx = value & 1;
            octets.add(new AsterixFspecOctet(
                    octetIndex++,
                    "%02X".formatted(value),
                    String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0'),
                    fx,
                    setBits.isEmpty() ? "-" : String.join(", ", setBits)
            ));

            if (fx == 0) {
                break;
            }
        }

        return octets;
    }

    private List<Integer> toUnsignedByteList(byte[] bytes) {
        List<Integer> values = new ArrayList<>(bytes.length);
        for (byte value : bytes) {
            values.add(Byte.toUnsignedInt(value));
        }
        return values;
    }

    private List<RawAsterixRecord> splitRecords(byte[] content) {
        List<RawAsterixRecord> records = new ArrayList<>();
        int offset = 0;

        while (offset + 3 <= content.length) {
            int category = Byte.toUnsignedInt(content[offset]);
            int length = (Byte.toUnsignedInt(content[offset + 1]) << 8) + Byte.toUnsignedInt(content[offset + 2]);
            if (length <= 0 || offset + length > content.length) {
                break;
            }

            byte[] recordBytes = new byte[length];
            System.arraycopy(content, offset, recordBytes, 0, length);
            records.add(new RawAsterixRecord(category, length, recordBytes));
            offset += length;
        }

        return records;
    }

    private String sanitizeFileName(String fileName) {
        String safe = safeText(fileName, "upload.ast").replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.length() > 40 ? safe.substring(safe.length() - 40) : safe;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "-";
        }

        int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
    }

    private record RawAsterixRecord(int category, int length, byte[] data) {
    }
}
