import { useState } from "react";
import { dependencies } from "@/infrastructure/container/dependencies";

interface UseEchoMessageState {
  reply: string | null;
  isLoading: boolean;
  error: string | null;
}

export function useEchoMessage() {
  const [state, setState] = useState<UseEchoMessageState>({
    reply: null,
    isLoading: false,
    error: null
  });

  async function send(message: string) {
    setState({
      reply: null,
      isLoading: true,
      error: null
    });

    try {
      const response = await dependencies.sendEchoMessageUseCase.execute(message);
      setState({
        reply: response.message,
        isLoading: false,
        error: null
      });
    } catch {
      setState({
        reply: null,
        isLoading: false,
        error: "Backend response could not be loaded."
      });
    }
  }

  return {
    ...state,
    send
  };
}
