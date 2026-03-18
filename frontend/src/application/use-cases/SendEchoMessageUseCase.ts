import type { EchoResponse } from "@/domain/entities/EchoResponse";
import type { MessageRepository } from "@/domain/repositories/MessageRepository";

export class SendEchoMessageUseCase {
  constructor(private readonly repository: MessageRepository) {}

  async execute(message: string): Promise<EchoResponse> {
    return this.repository.echo(message);
  }
}
