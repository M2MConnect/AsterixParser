export type SystemStatusLevel = "online" | "degraded" | "offline";

export interface SystemStatus {
  title: string;
  description: string;
  level: SystemStatusLevel;
  updatedAt: string;
}
