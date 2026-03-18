package backend.domain.model;

import java.util.List;

public record AsterixCategoryPage(
        String categoryKey,
        int currentPage,
        int pageSize,
        int totalPages,
        int totalRecords,
        List<AsterixRecordAnalysis> records
) {
}
