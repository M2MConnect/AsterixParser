import type { AsterixAnalysisResult } from "@/domain/entities/AsterixAnalysisResult";
import type { AsterixCategoryPage } from "@/domain/entities/AsterixCategoryPage";
import type { AsterixSampleFile } from "@/domain/entities/AsterixSampleFile";
import type { EchoResponse } from "@/domain/entities/EchoResponse";
import type { SystemStatus } from "@/domain/entities/SystemStatus";
import type { AsterixRepository } from "@/domain/repositories/AsterixRepository";
import type { MessageRepository } from "@/domain/repositories/MessageRepository";
import type { SystemStatusRepository } from "@/domain/repositories/SystemStatusRepository";

const API_BASE_URL = "http://localhost:8080/api";

export class HttpBackendRepository
  implements SystemStatusRepository, MessageRepository, AsterixRepository
{
  async getCurrentStatus(): Promise<SystemStatus> {
    const response = await fetch(`${API_BASE_URL}/status`);

    if (!response.ok) {
      throw new Error("Status could not be loaded.");
    }

    return response.json() as Promise<SystemStatus>;
  }

  async echo(message: string): Promise<EchoResponse> {
    const response = await fetch(`${API_BASE_URL}/messages/echo`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ message })
    });

    if (!response.ok) {
      throw new Error("Message could not be sent.");
    }

    return response.json() as Promise<EchoResponse>;
  }

  async analyze(file: File): Promise<AsterixAnalysisResult> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API_BASE_URL}/asterix/analyze`, {
      method: "POST",
      body: formData
    });

    if (!response.ok) {
      const payload = (await response.json().catch(() => null)) as { error?: string } | null;
      throw new Error(payload?.error ?? "ASTERIX file could not be analyzed.");
    }

    return response.json() as Promise<AsterixAnalysisResult>;
  }

  async listSamples(): Promise<AsterixSampleFile[]> {
    const response = await fetch(`${API_BASE_URL}/asterix/samples`);

    if (!response.ok) {
      throw new Error("Sample files could not be loaded.");
    }

    return response.json() as Promise<AsterixSampleFile[]>;
  }

  async analyzeSample(sampleId: string): Promise<AsterixAnalysisResult> {
    const response = await fetch(`${API_BASE_URL}/asterix/samples/${sampleId}/analyze`, {
      method: "POST"
    });

    if (!response.ok) {
      const payload = (await response.json().catch(() => null)) as { error?: string } | null;
      throw new Error(payload?.error ?? "Sample file could not be analyzed.");
    }

    return response.json() as Promise<AsterixAnalysisResult>;
  }

  async getCategoryPage(
    analysisId: string,
    categoryKey: string,
    page: number,
    size: number
  ): Promise<AsterixCategoryPage> {
    const response = await fetch(
      `${API_BASE_URL}/asterix/analyses/${analysisId}/categories/${categoryKey}/records?page=${page}&size=${size}`
    );

    if (!response.ok) {
      const payload = (await response.json().catch(() => null)) as { error?: string } | null;
      throw new Error(payload?.error ?? "Record page could not be loaded.");
    }

    return response.json() as Promise<AsterixCategoryPage>;
  }
}
