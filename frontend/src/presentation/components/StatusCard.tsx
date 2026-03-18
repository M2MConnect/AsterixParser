import type { SystemStatus } from "@/domain/entities/SystemStatus";

interface StatusCardProps {
  status: SystemStatus;
}

export function StatusCard({ status }: StatusCardProps) {
  return (
    <section className="status-card">
      <div className="status-card__header">
        <span className={`status-pill status-pill--${status.level}`}>
          {status.level}
        </span>
        <span className="status-card__time">
          Updated: {new Date(status.updatedAt).toLocaleString("en-US")}
        </span>
      </div>

      <h2>{status.title}</h2>
      <p>{status.description}</p>
    </section>
  );
}
