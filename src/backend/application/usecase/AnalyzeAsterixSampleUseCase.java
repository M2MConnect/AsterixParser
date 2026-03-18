package backend.application.usecase;

import backend.domain.model.AsterixAnalysisResult;
import backend.domain.model.AsterixSampleFile;
import backend.infrastructure.storage.AsterixAnalysisSession;
import backend.infrastructure.storage.AsterixAnalysisSessionStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AnalyzeAsterixSampleUseCase {

    private final AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase;
    private final ListAsterixSamplesUseCase listAsterixSamplesUseCase;
    private final AsterixAnalysisSessionStore sessionStore;
    private final int pageSize;

    public AnalyzeAsterixSampleUseCase(
            AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase,
            ListAsterixSamplesUseCase listAsterixSamplesUseCase,
            AsterixAnalysisSessionStore sessionStore,
            int pageSize
    ) {
        this.analyzeAsterixFileUseCase = analyzeAsterixFileUseCase;
        this.listAsterixSamplesUseCase = listAsterixSamplesUseCase;
        this.sessionStore = sessionStore;
        this.pageSize = pageSize;
    }

    public AsterixAnalysisResult execute(String sampleId) {
        List<AsterixSampleFile> samples = listAsterixSamplesUseCase.execute();
        AsterixSampleFile sample = samples.stream()
                .filter(item -> item.id().equals(sampleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sample nicht gefunden: " + sampleId));

        Resource resource = new ClassPathResource("samples/" + sample.fileName());

        try (InputStream inputStream = resource.getInputStream()) {
            Path tempFile = Files.createTempFile("asterix-sample-", resolveExtension(sample.fileName()));
            Files.write(tempFile, inputStream.readAllBytes());
            AsterixAnalysisSession session = sessionStore.create(tempFile, sample.fileName(), sample.fileSizeBytes());
            return analyzeAsterixFileUseCase.execute(
                    session.analysisId(),
                    session.filePath(),
                    session.fileName(),
                    session.fileSizeBytes(),
                    pageSize
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Sample-Datei konnte nicht gelesen werden: " + sample.fileName(), exception);
        }
    }

    private String resolveExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == fileName.length() - 1) {
            return ".bin";
        }
        return fileName.substring(extensionIndex);
    }
}
