package backend.domain.model;

import java.util.List;

public record AsterixAnalysisResult(
        String analysisId,
        String fileName,
        long fileSizeBytes,
        String analyzedAt,
        AsterixSummary summary,
        List<AsterixCategoryAnalysis> categories,
        AsterixAnalysisVisualization visualization
) {
}
