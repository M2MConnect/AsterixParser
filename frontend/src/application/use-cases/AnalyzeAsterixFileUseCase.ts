import type { AsterixAnalysisResult } from "@/domain/entities/AsterixAnalysisResult";
import type { AsterixRepository } from "@/domain/repositories/AsterixRepository";

export class AnalyzeAsterixFileUseCase {
  constructor(private readonly repository: AsterixRepository) {}

  async execute(file: File): Promise<AsterixAnalysisResult> {
    return this.repository.analyze(file);
  }
}
