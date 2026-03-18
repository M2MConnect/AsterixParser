package backend.domain.model;

import java.util.List;

public record AsterixAnalysisVisualization(
        List<AsterixCategoryDistributionItem> categoryDistribution,
        List<AsterixTrafficTimelineBucket> trafficTimeline
) {
}
