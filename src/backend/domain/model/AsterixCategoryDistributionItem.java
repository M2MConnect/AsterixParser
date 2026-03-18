package backend.domain.model;

public record AsterixCategoryDistributionItem(
        String categoryKey,
        String label,
        int count,
        double percentage
) {
}
