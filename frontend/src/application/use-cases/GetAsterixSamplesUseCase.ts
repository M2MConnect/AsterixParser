import type { AsterixSampleFile } from "@/domain/entities/AsterixSampleFile";
import type { AsterixRepository } from "@/domain/repositories/AsterixRepository";

export class GetAsterixSamplesUseCase {
  constructor(private readonly repository: AsterixRepository) {}

  async execute(): Promise<AsterixSampleFile[]> {
    return this.repository.listSamples();
  }
}
