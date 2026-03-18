import type { EchoResponse } from "@/domain/entities/EchoResponse";

export interface MessageRepository {
  echo(message: string): Promise<EchoResponse>;
}
