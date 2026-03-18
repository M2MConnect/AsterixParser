import { useState } from "react";
import type { AsterixCategoryPage } from "@/domain/entities/AsterixCategoryPage";
import type { AsterixAnalysisResult } from "@/domain/entities/AsterixAnalysisResult";
import { dependencies } from "@/infrastructure/container/dependencies";

interface UseAsterixAnalysisState {
  data: AsterixAnalysisResult | null;
  isLoading: boolean;
  error: string | null;
}

export function useAsterixAnalysis() {
  const [state, setState] = useState<UseAsterixAnalysisState>({
    data: null,
    isLoading: false,
    error: null
  });

  async function analyze(file: File) {
    setState({
      data: null,
      isLoading: true,
      error: null
    });

    try {
      const response = await dependencies.analyzeAsterixFileUseCase.execute(file);
      setState({
        data: response,
        isLoading: false,
        error: null
      });
    } catch (error) {
      setState({
        data: null,
        isLoading: false,
        error: error instanceof Error ? error.message : "Analysis could not be started."
      });
    }
  }

  async function analyzeSample(sampleId: string) {
    setState({
      data: null,
      isLoading: true,
      error: null
    });

    try {
      const response = await dependencies.analyzeAsterixSampleUseCase.execute(sampleId);
      setState({
        data: response,
        isLoading: false,
        error: null
      });
    } catch (error) {
      setState({
        data: null,
        isLoading: false,
        error: error instanceof Error ? error.message : "Analysis could not be started."
      });
    }
  }

  async function getCategoryPage(
    analysisId: string,
    categoryKey: string,
    page: number,
    size: number
  ): Promise<AsterixCategoryPage> {
    return dependencies.getAsterixCategoryPageUseCase.execute(analysisId, categoryKey, page, size);
  }

  return {
    ...state,
    analyze,
    analyzeSample,
    getCategoryPage
  };
}
