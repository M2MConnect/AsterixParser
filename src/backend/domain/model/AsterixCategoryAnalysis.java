package backend.domain.model;

import java.util.List;

public record AsterixCategoryAnalysis(
        int category,
        String categoryKey,
        int count,
        String description,
        String defaultEdition,
        String definitionFileName,
        String uapItems,
        int currentPage,
        int pageSize,
        int totalPages,
        List<AsterixRecordAnalysis> records
) {
}
