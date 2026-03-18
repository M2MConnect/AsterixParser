import { useEffect, useState } from "react";
import type { AsterixSampleFile } from "@/domain/entities/AsterixSampleFile";
import { dependencies } from "@/infrastructure/container/dependencies";

interface UseAsterixSamplesState {
  data: AsterixSampleFile[];
  isLoading: boolean;
  error: string | null;
}

export function useAsterixSamples() {
  const [state, setState] = useState<UseAsterixSamplesState>({
    data: [],
    isLoading: true,
    error: null
  });

  useEffect(() => {
    let isMounted = true;

    dependencies.getAsterixSamplesUseCase
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
          data: [],
          isLoading: false,
          error: "Sample files could not be loaded."
        });
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return state;
}
