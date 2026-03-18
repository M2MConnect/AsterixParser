import type { SystemStatus } from "@/domain/entities/SystemStatus";

export interface SystemStatusRepository {
  getCurrentStatus(): Promise<SystemStatus>;
}
