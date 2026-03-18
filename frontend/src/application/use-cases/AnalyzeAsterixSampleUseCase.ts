import type { AsterixAnalysisResult } from "@/domain/entities/AsterixAnalysisResult";
import type { AsterixRepository } from "@/domain/repositories/AsterixRepository";

export class AnalyzeAsterixSampleUseCase {
  constructor(private readonly repository: AsterixRepository) {}

  async execute(sampleId: string): Promise<AsterixAnalysisResult> {
    return this.repository.analyzeSample(sampleId);
  }
}
