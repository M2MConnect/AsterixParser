package backend.domain.model;

public record SystemStatus(
        String title,
        String description,
        String level,
        String updatedAt
) {
}
