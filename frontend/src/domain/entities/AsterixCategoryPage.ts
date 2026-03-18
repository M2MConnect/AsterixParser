import type { AsterixRecordAnalysis } from "@/domain/entities/AsterixAnalysisResult";

export interface AsterixCategoryPage {
  categoryKey: string;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
  records: AsterixRecordAnalysis[];
}
