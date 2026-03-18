package backend.domain.model;

import java.util.List;

public record AsterixTrafficTimelineBucket(
        int bucketIndex,
        int startRecordIndex,
        int endRecordIndex,
        int totalRecords,
        List<AsterixTimelineCategoryCount> categories
) {
}
