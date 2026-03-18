import { useEffect, useRef, useState } from "react";
import type {
  AsterixCategoryAnalysis,
  AsterixCategoryDistributionItem,
  AsterixFspecOctet,
  AsterixRecordAnalysis,
  AsterixTrafficTimelineBucket,
  AsterixUapItem
} from "@/domain/entities/AsterixAnalysisResult";
import type { AsterixCategoryPage } from "@/domain/entities/AsterixCategoryPage";
import type { AsterixSampleFile } from "@/domain/entities/AsterixSampleFile";
import { useAsterixAnalysis } from "@/presentation/hooks/useAsterixAnalysis";
import { useAsterixSamples } from "@/presentation/hooks/useAsterixSamples";

type ByteRow = {
  offset: number;
  cells: Array<{ offset: number; value: number } | null>;
};

const CATEGORIES_PER_PAGE = 4;

export function DashboardPage() {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [selectedFileName, setSelectedFileName] = useState("No file selected.");
  const [showFspecDetails, setShowFspecDetails] = useState(false);
  const [categoryPage, setCategoryPage] = useState(1);
  const { data, isLoading, error, analyze, analyzeSample, getCategoryPage } = useAsterixAnalysis();
  const samples = useAsterixSamples();

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setSelectedFileName(file.name);
    setCategoryPage(1);
    await analyze(file);
  }

  async function handleSampleAnalyze(sample: AsterixSampleFile) {
    setSelectedFileName(sample.fileName);
    setCategoryPage(1);
    await analyzeSample(sample.id);
  }

  const totalCategoryPages = data ? Math.max(1, Math.ceil(data.categories.length / CATEGORIES_PER_PAGE)) : 1;
  const pagedCategories = data
    ? data.categories.slice((categoryPage - 1) * CATEGORIES_PER_PAGE, categoryPage * CATEGORIES_PER_PAGE)
    : [];

  return (
    <main className="page-shell">
      <section className="hero hero--asterix">
        <p className="eyebrow">ASTERIX Frame Inspector</p>
        <h1>ASTERIX Analysis Platform</h1>
        <p className="hero-copy">
          The application processes ASTERIX files through an integrated analysis pipeline
          and presents structured results with byte, FSPEC, and UAP views.
        </p>
        <div className="upload-panel">
          <input
            ref={inputRef}
            className="upload-input"
            type="file"
            accept=".ast,.bin,.raw,.dat"
            onChange={handleFileChange}
          />
          <button className="upload-button" type="button" onClick={() => inputRef.current?.click()}>
            Choose file
          </button>
          <span className="upload-filename">{selectedFileName}</span>
          <label className="upload-toggle">
            <input
              type="checkbox"
              checked={showFspecDetails}
              onChange={(event) => setShowFspecDetails(event.target.checked)}
            />
            Show FSPEC details
          </label>
        </div>
        <div className="sample-panel">
          <div className="sample-panel__header">
            <strong>Sample files in the Java project</strong>
            {samples.isLoading && <span>loading...</span>}
            {samples.error && <span>{samples.error}</span>}
          </div>
          <div className="sample-list">
            {samples.data.map((sample) => (
              <button
                key={sample.id}
                className="sample-button"
                type="button"
                onClick={() => handleSampleAnalyze(sample)}
                disabled={isLoading}
              >
                <span>{sample.fileName}</span>
                <small>{formatNumber(sample.fileSizeBytes)} Bytes | {sample.description}</small>
              </button>
            ))}
          </div>
        </div>
      </section>

      <section className="grid grid--single">
        <div className="panel">
          <h2>Status</h2>
          {!isLoading && !error && !data && (
            <p className="panel-copy">Please select an ASTERIX file.</p>
          )}
          {isLoading && <p className="panel-copy">Analysis in progress. This may take some time for large files.</p>}
          {error && <p className="echo-error">{error}</p>}

          {data && (
            <>
              <div className="summary-grid">
                <article className="summary-card">
                  <span>File</span>
                  <strong>{data.fileName}</strong>
                  <small>{formatNumber(data.fileSizeBytes)} Bytes</small>
                </article>
                <article className="summary-card">
                  <span>Records</span>
                  <strong>{formatNumber(data.summary.totalRecords)}</strong>
                  <small>{data.summary.detectedCategories} Categories</small>
                </article>
                <article className="summary-card">
                  <span>CAT10 / CAT21</span>
                  <strong>
                    {formatNumber(data.summary.cat10Records)} / {formatNumber(data.summary.cat21Records)}
                  </strong>
                  <small>{formatNumber(data.summary.flights)} Flights</small>
                </article>
                <article className="summary-card">
                  <span>Loaded preview</span>
                  <strong>{data.summary.previewLimit} records per category page</strong>
                  <small>{new Date(data.analyzedAt).toLocaleString("en-US")}</small>
                </article>
              </div>

              <div className="insight-grid">
                <CategoryDistributionCard items={data.visualization.categoryDistribution} />
                <TrafficTimelineCard buckets={data.visualization.trafficTimeline} />
              </div>

              {data.categories.length === 0 && (
                <p className="panel-copy">No valid ASTERIX records found.</p>
              )}

              {data.categories.length > CATEGORIES_PER_PAGE && (
                <PaginationBar
                  label="Categories"
                  currentPage={categoryPage}
                  totalPages={totalCategoryPages}
                  onPageChange={setCategoryPage}
                />
              )}

              <div className="category-list">
                {pagedCategories.map((category) => (
                  <CategoryCard
                    key={category.categoryKey}
                    analysisId={data.analysisId}
                    category={category}
                    showFspecDetails={showFspecDetails}
                    loadCategoryPage={getCategoryPage}
                  />
                ))}
              </div>

              {data.categories.length > CATEGORIES_PER_PAGE && (
                <PaginationBar
                  label="Categories"
                  currentPage={categoryPage}
                  totalPages={totalCategoryPages}
                  onPageChange={setCategoryPage}
                />
              )}
            </>
          )}
        </div>
      </section>
    </main>
  );
}

function CategoryDistributionCard({ items }: { items: AsterixCategoryDistributionItem[] }) {
  const maxCount = Math.max(...items.map((item) => item.count), 1);

  return (
    <section className="insight-card">
      <div className="insight-card__header">
        <div>
          <p className="insight-card__kicker">Distribution</p>
          <h3>Category shares</h3>
        </div>
        <span>{items.length} Categories</span>
      </div>

      {items.length === 0 ? (
        <p className="panel-copy">No category data available.</p>
      ) : (
        <div className="distribution-list">
          {items.map((item) => (
            <div className="distribution-row" key={item.categoryKey}>
              <div className="distribution-row__meta">
                <strong>{item.label}</strong>
                <span>
                  {formatNumber(item.count)} Records | {formatPercent(item.percentage)}
                </span>
              </div>
              <div className="distribution-bar">
                <div
                  className="distribution-bar__fill"
                  style={{ width: `${Math.max(6, (item.count / maxCount) * 100)}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function TrafficTimelineCard({ buckets }: { buckets: AsterixTrafficTimelineBucket[] }) {
  const maxRecords = Math.max(...buckets.map((bucket) => bucket.totalRecords), 1);

  return (
    <section className="insight-card">
      <div className="insight-card__header">
        <div>
          <p className="insight-card__kicker">Timeline</p>
          <h3>Message flow</h3>
        </div>
        <span>{buckets.length} Segments</span>
      </div>

      {buckets.length === 0 ? (
        <p className="panel-copy">No timeline data available.</p>
      ) : (
        <>
          <div className="timeline-chart" aria-label="Timeline of record density across the file stream">
            {buckets.map((bucket) => (
              <div className="timeline-bar-wrap" key={bucket.bucketIndex} title={buildBucketTitle(bucket)}>
                <div
                  className="timeline-bar"
                  style={{ height: `${Math.max(12, (bucket.totalRecords / maxRecords) * 160)}px` }}
                />
              </div>
            ))}
          </div>
          <div className="timeline-scale">
            <span>Start</span>
            <span>File timeline</span>
            <span>End</span>
          </div>
          <div className="timeline-legend">
            {buckets.slice(0, 4).map((bucket) => (
              <div className="timeline-legend__item" key={bucket.bucketIndex}>
                <strong>Segment {bucket.bucketIndex}</strong>
                <span>{buildBucketSummary(bucket)}</span>
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function CategoryCard({
  analysisId,
  category,
  showFspecDetails,
  loadCategoryPage
}: {
  analysisId: string;
  category: AsterixCategoryAnalysis;
  showFspecDetails: boolean;
  loadCategoryPage: (analysisId: string, categoryKey: string, page: number, size: number) => Promise<{
    currentPage: number;
    pageSize: number;
    totalPages: number;
    totalRecords: number;
    records: AsterixRecordAnalysis[];
  }>;
}) {
  const [recordPage, setRecordPage] = useState(category.currentPage);
  const [records, setRecords] = useState(category.records);
  const [totalRecordPages, setTotalRecordPages] = useState(category.totalPages);
  const [pageSize, setPageSize] = useState(category.pageSize);
  const [isPageLoading, setIsPageLoading] = useState(false);
  const [pageCache, setPageCache] = useState<Record<number, AsterixRecordAnalysis[]>>({
    [category.currentPage]: category.records
  });

  useEffect(() => {
    setRecordPage(category.currentPage);
    setRecords(category.records);
    setTotalRecordPages(category.totalPages);
    setPageSize(category.pageSize);
    setPageCache({
      [category.currentPage]: category.records
    });
  }, [category]);

  async function handleRecordPageChange(page: number) {
    if (page === recordPage || page < 1 || page > totalRecordPages) {
      return;
    }

    if (pageCache[page]) {
      setRecordPage(page);
      setRecords(pageCache[page]);
      return;
    }

    setIsPageLoading(true);
    try {
      const nextPage = await loadCategoryPage(analysisId, category.categoryKey, page, pageSize);
      setRecordPage(nextPage.currentPage);
      setTotalRecordPages(nextPage.totalPages);
      setRecords(nextPage.records);
      setPageCache((current) => ({
        ...current,
        [nextPage.currentPage]: nextPage.records
      }));
    } finally {
      setIsPageLoading(false);
    }
  }

  return (
    <section className="cat-card">
      <div className="cat-head">
        <div>
          <p className="cat-kicker">Category</p>
          <h3>CAT{category.categoryKey}</h3>
        </div>
        <div className="cat-badge">{formatNumber(category.count)} Records</div>
      </div>

      <div className="cat-meta">
        <div>
          <span>Description</span>
          <strong>{category.description}</strong>
        </div>
        <div>
          <span>Edition</span>
          <strong>{category.defaultEdition}</strong>
        </div>
        <div>
          <span>Definition</span>
          <strong>{category.definitionFileName}</strong>
        </div>
        <div>
          <span>UAP-Items</span>
          <strong>{category.uapItems || "-"}</strong>
        </div>
      </div>

      <div className="record-list">
        {category.count > pageSize && (
          <PaginationBar
            label={`Records CAT${category.categoryKey}`}
            currentPage={recordPage}
            totalPages={totalRecordPages}
            onPageChange={handleRecordPageChange}
            isLoading={isPageLoading}
          />
        )}

        {records.map((record) => (
          <RecordCard key={`${category.categoryKey}-${record.index}`} record={record} showFspecDetails={showFspecDetails} />
        ))}

        {category.count > pageSize && (
          <PaginationBar
            label={`Records CAT${category.categoryKey}`}
            currentPage={recordPage}
            totalPages={totalRecordPages}
            onPageChange={handleRecordPageChange}
            isLoading={isPageLoading}
          />
        )}
      </div>
    </section>
  );
}

function PaginationBar({
  label,
  currentPage,
  totalPages,
  onPageChange,
  isLoading = false
}: {
  label: string;
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  isLoading?: boolean;
}) {
  return (
    <div className="pagination-bar">
      <span className="pagination-label">
        {label}: Page {currentPage} / {totalPages}
      </span>
      <div className="pagination-actions">
        <button
          className="pagination-button"
          type="button"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage <= 1 || isLoading}
        >
          Back
        </button>
        <button
          className="pagination-button"
          type="button"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages || isLoading}
        >
          {isLoading ? "Loading..." : "Next"}
        </button>
      </div>
    </div>
  );
}

function RecordCard({
  record,
  showFspecDetails
}: {
  record: AsterixRecordAnalysis;
  showFspecDetails: boolean;
}) {
  const byteRows = buildByteRows(record.rawBytes);
  const fspecLength = calculateFspecLength(record.rawBytes);

  return (
    <article className="record-card">
      <div className="record-title-row">
        <h4>Record {record.index}</h4>
        <span className="record-pill">LEN {record.length}</span>
      </div>
      <p className="record-copy">
        FSPEC={record.fspec} | Items={record.itemList} | Remaining={record.remainingBytes}
      </p>

      <div className="legend">
        <span className="legend-chip legend-chip--header">Header</span>
        <span className="legend-chip legend-chip--fspec">FSPEC</span>
      </div>

      <div className="frame-wrap">
        <table className="frame">
          <thead>
            <tr>
              <th>Off</th>
              {Array.from({ length: 16 }, (_, index) => (
                <th key={index}>{index.toString(16).toUpperCase().padStart(2, "0")}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {byteRows.map((row) => (
              <tr key={row.offset}>
                <td className="offset-cell">{row.offset.toString(16).toUpperCase().padStart(4, "0")}</td>
                {row.cells.map((cell, cellIndex) => {
                  if (cell === null) {
                    return <td className="byte-empty" key={`${row.offset}-${cellIndex}`} />;
                  }

                  return (
                    <td className={getByteCellClass(cell.offset, fspecLength)} key={`${row.offset}-${cell.offset}`}>
                      <div className="hex">{cell.value.toString(16).toUpperCase().padStart(2, "0")}</div>
                      <div className="dec">{cell.value}</div>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showFspecDetails && <FspecTable octets={record.fspecOctets} />}
      <UapTable items={record.uapItems} />
    </article>
  );
}

function FspecTable({ octets }: { octets: AsterixFspecOctet[] }) {
  return (
    <div className="detail-panel">
      <div className="detail-title">FSPEC Detail</div>
      <table className="detail-table">
        <thead>
          <tr>
            <th>Octet</th>
            <th>Hex</th>
            <th>Bin</th>
            <th>FX</th>
            <th>Set bits</th>
          </tr>
        </thead>
        <tbody>
          {octets.map((octet) => (
            <tr key={octet.octetIndex}>
              <td>{octet.octetIndex}</td>
              <td>{octet.hexValue}</td>
              <td>{octet.binaryValue}</td>
              <td>{octet.fxValue}</td>
              <td>{octet.setBits}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function UapTable({ items }: { items: AsterixUapItem[] }) {
  return (
    <div className="detail-panel">
      <div className="detail-title">UAP Items</div>
      {items.length === 0 ? (
        <p className="panel-copy">No UAP items decoded.</p>
      ) : (
        <table className="detail-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Description</th>
              <th>Value</th>
              <th>Bytes</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={`${item.id}-${index}`}>
                <td>{item.id}</td>
                <td>{item.name}</td>
                <td>{item.comment}</td>
                <td className="value-preview">{item.valuePreview}</td>
                <td>{item.consumedBytes}</td>
                <td className={getStatusClass(item.status)}>{item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function buildByteRows(rawBytes: number[]): ByteRow[] {
  const rows: ByteRow[] = [];

  for (let offset = 0; offset < rawBytes.length; offset += 16) {
    const cells = Array.from({ length: 16 }, (_, col) => {
      const index = offset + col;
      if (index >= rawBytes.length) {
        return null;
      }

      return {
        offset: index,
        value: rawBytes[index]
      };
    });

    rows.push({ offset, cells });
  }

  return rows;
}

function calculateFspecLength(rawBytes: number[]) {
  let offset = 3;
  let count = 0;

  while (offset < rawBytes.length) {
    const value = rawBytes[offset];
    count += 1;
    offset += 1;
    if ((value & 0x01) === 0) {
      break;
    }
  }

  return count;
}

function getByteCellClass(offset: number, fspecLength: number) {
  let className = "byte-cell";
  if (offset <= 2) {
    className += " byte-cell--header";
  } else if (offset >= 3 && offset < 3 + fspecLength) {
    className += " byte-cell--fspec";
  }

  return className;
}

function getStatusClass(status: string) {
  switch (status) {
    case "OK":
      return "status-ok";
    case "LIMITED":
    case "MISSING_DEF":
    case "PARTIAL":
      return "status-limited";
    case "ERROR":
      return "status-error";
    default:
      return "status-empty";
  }
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-US").format(value);
}

function formatPercent(value: number) {
  return (
    new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 1,
      maximumFractionDigits: 1
    }).format(value) + " %"
  );
}

function buildBucketTitle(bucket: AsterixTrafficTimelineBucket) {
  return `Segment ${bucket.bucketIndex}: Records ${formatNumber(bucket.startRecordIndex + 1)} to ${formatNumber(bucket.endRecordIndex + 1)} | ${formatNumber(bucket.totalRecords)} Records | ${buildBucketSummary(bucket)}`;
}

function buildBucketSummary(bucket: AsterixTrafficTimelineBucket) {
  const topCategories = bucket.categories.slice(0, 3);
  if (topCategories.length === 0) {
    return "No categories";
  }

  return topCategories
    .map((category) => `CAT${category.categoryKey} ${formatNumber(category.count)}`)
    .join(" | ");
}
