package backend.domain.model;

public record AsterixUapItem(
        String id,
        String name,
        String comment,
        String valuePreview,
        int startByteOffset,
        int consumedBytes,
        int endByteOffset,
        String status
) {
}
