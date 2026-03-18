import type { AsterixAnalysisResult } from "@/domain/entities/AsterixAnalysisResult";
import type { AsterixCategoryPage } from "@/domain/entities/AsterixCategoryPage";
import type { AsterixSampleFile } from "@/domain/entities/AsterixSampleFile";

export interface AsterixRepository {
  analyze(file: File): Promise<AsterixAnalysisResult>;
  listSamples(): Promise<AsterixSampleFile[]>;
  analyzeSample(sampleId: string): Promise<AsterixAnalysisResult>;
  getCategoryPage(analysisId: string, categoryKey: string, page: number, size: number): Promise<AsterixCategoryPage>;
}
