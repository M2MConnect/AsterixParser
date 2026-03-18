package backend.domain.model;

public record AsterixUapItem(
        String id,
        String name,
        String comment,
        String valuePreview,
        int consumedBytes,
        String status
) {
}
