export interface AsterixSummary {
  totalRecords: number;
  detectedCategories: number;
  cat10Records: number;
  cat21Records: number;
  flights: number;
  previewLimit: number;
}

export interface AsterixFspecOctet {
  octetIndex: number;
  hexValue: string;
  binaryValue: string;
  fxValue: number;
  definition: string;
}

export interface AsterixUapItem {
  id: string;
  name: string;
  comment: string;
  valuePreview: string;
  startByteOffset: number;
  consumedBytes: number;
  endByteOffset: number;
  status: string;
}

export interface AsterixRecordAnalysis {
  index: number;
  length: number;
  fspec: string;
  remainingBytes: number;
  itemList: string;
  rawBytes: number[];
  fspecOctets: AsterixFspecOctet[];
  uapItems: AsterixUapItem[];
}

export interface AsterixCategoryAnalysis {
  category: number;
  categoryKey: string;
  count: number;
  description: string;
  defaultEdition: string;
  definitionFileName: string;
  uapItems: string;
  currentPage: number;
  pageSize: number;
  totalPages: number;
  records: AsterixRecordAnalysis[];
}

export interface AsterixCategoryDistributionItem {
  categoryKey: string;
  label: string;
  count: number;
  percentage: number;
}

export interface AsterixTimelineCategoryCount {
  categoryKey: string;
  count: number;
}

export interface AsterixTrafficTimelineBucket {
  bucketIndex: number;
  startRecordIndex: number;
  endRecordIndex: number;
  totalRecords: number;
  categories: AsterixTimelineCategoryCount[];
}

export interface AsterixAnalysisVisualization {
  categoryDistribution: AsterixCategoryDistributionItem[];
  trafficTimeline: AsterixTrafficTimelineBucket[];
}

export interface AsterixAnalysisResult {
  analysisId: string;
  fileName: string;
  fileSizeBytes: number;
  analyzedAt: string;
  summary: AsterixSummary;
  categories: AsterixCategoryAnalysis[];
  visualization: AsterixAnalysisVisualization;
}
