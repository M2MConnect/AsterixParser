package backend.domain.model;

public record AsterixSummary(
        int totalRecords,
        int detectedCategories,
        int cat10Records,
        int cat21Records,
        int flights,
        int previewLimit
) {
}
