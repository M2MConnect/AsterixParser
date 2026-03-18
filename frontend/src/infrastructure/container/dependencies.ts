import { AnalyzeAsterixFileUseCase } from "@/application/use-cases/AnalyzeAsterixFileUseCase";
import { GetAsterixCategoryPageUseCase } from "@/application/use-cases/GetAsterixCategoryPageUseCase";
import { AnalyzeAsterixSampleUseCase } from "@/application/use-cases/AnalyzeAsterixSampleUseCase";
import { GetAsterixSamplesUseCase } from "@/application/use-cases/GetAsterixSamplesUseCase";
import { SendEchoMessageUseCase } from "@/application/use-cases/SendEchoMessageUseCase";
import { GetSystemStatusUseCase } from "@/application/use-cases/GetSystemStatusUseCase";
import { HttpBackendRepository } from "@/infrastructure/data/httpBackendRepository";

const backendRepository = new HttpBackendRepository();

export const dependencies = {
  analyzeAsterixFileUseCase: new AnalyzeAsterixFileUseCase(backendRepository),
  analyzeAsterixSampleUseCase: new AnalyzeAsterixSampleUseCase(backendRepository),
  getAsterixCategoryPageUseCase: new GetAsterixCategoryPageUseCase(backendRepository),
  getAsterixSamplesUseCase: new GetAsterixSamplesUseCase(backendRepository),
  getSystemStatusUseCase: new GetSystemStatusUseCase(backendRepository),
  sendEchoMessageUseCase: new SendEchoMessageUseCase(backendRepository)
};
