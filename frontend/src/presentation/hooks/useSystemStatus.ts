import { useEffect, useState } from "react";
import type { SystemStatus } from "@/domain/entities/SystemStatus";
import { dependencies } from "@/infrastructure/container/dependencies";

interface UseSystemStatusState {
  data: SystemStatus | null;
  isLoading: boolean;
  error: string | null;
}

export function useSystemStatus() {
  const [state, setState] = useState<UseSystemStatusState>({
    data: null,
    isLoading: true,
    error: null
  });

  useEffect(() => {
    let isMounted = true;

    dependencies.getSystemStatusUseCase
      .execute()
      .then((data) => {
        if (!isMounted) {
          return;
        }

        setState({
          data,
          isLoading: false,
          error: null
        });
      })
      .catch(() => {
        if (!isMounted) {
          return;
        }

        setState({
          data: null,
          isLoading: false,
          error: "Status could not be loaded."
        });
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return state;
}
