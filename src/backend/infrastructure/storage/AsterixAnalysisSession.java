package backend.infrastructure.storage;

import java.nio.file.Path;

public record AsterixAnalysisSession(
        String analysisId,
        Path filePath,
        String fileName,
        long fileSizeBytes
) {
}
