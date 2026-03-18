import type { AsterixCategoryPage } from "@/domain/entities/AsterixCategoryPage";
import type { AsterixRepository } from "@/domain/repositories/AsterixRepository";

export class GetAsterixCategoryPageUseCase {
  constructor(private readonly repository: AsterixRepository) {}

  async execute(
    analysisId: string,
    categoryKey: string,
    page: number,
    size: number
  ): Promise<AsterixCategoryPage> {
    return this.repository.getCategoryPage(analysisId, categoryKey, page, size);
  }
}
