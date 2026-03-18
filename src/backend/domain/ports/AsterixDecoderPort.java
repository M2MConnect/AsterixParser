package backend.domain.ports;

import com.fasterxml.jackson.databind.JsonNode;

public interface AsterixDecoderPort {

    JsonNode analyze(String inputFilePath, int previewLimit);
}
