package backend.domain.model;

public record AsterixSampleFile(
        String id,
        String fileName,
        long fileSizeBytes,
        String description
) {
}
