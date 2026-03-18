import type { SystemStatus } from "@/domain/entities/SystemStatus";
import type { SystemStatusRepository } from "@/domain/repositories/SystemStatusRepository";

export class GetSystemStatusUseCase {
  constructor(private readonly repository: SystemStatusRepository) {}

  async execute(): Promise<SystemStatus> {
    return this.repository.getCurrentStatus();
  }
}
