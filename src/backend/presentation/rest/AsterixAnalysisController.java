package backend.presentation.rest;

import backend.application.usecase.AnalyzeAsterixFileUseCase;
import backend.application.usecase.AnalyzeAsterixSampleUseCase;
import backend.application.usecase.ListAsterixSamplesUseCase;
import backend.domain.model.AsterixAnalysisResult;
import backend.domain.model.AsterixCategoryPage;
import backend.domain.model.AsterixSampleFile;
import backend.infrastructure.storage.AsterixAnalysisSession;
import backend.infrastructure.storage.AsterixAnalysisSessionStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/asterix")
@Tag(name = "ASTERIX")
public class AsterixAnalysisController {

    private final AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase;
    private final ListAsterixSamplesUseCase listAsterixSamplesUseCase;
    private final AnalyzeAsterixSampleUseCase analyzeAsterixSampleUseCase;
    private final AsterixAnalysisSessionStore sessionStore;
    private final int pageSize;

    public AsterixAnalysisController(
            AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase,
            ListAsterixSamplesUseCase listAsterixSamplesUseCase,
            AnalyzeAsterixSampleUseCase analyzeAsterixSampleUseCase,
            AsterixAnalysisSessionStore sessionStore,
            @org.springframework.beans.factory.annotation.Value("${asterix.decoder.preview-limit:10}") int pageSize
    ) {
        this.analyzeAsterixFileUseCase = analyzeAsterixFileUseCase;
        this.listAsterixSamplesUseCase = listAsterixSamplesUseCase;
        this.analyzeAsterixSampleUseCase = analyzeAsterixSampleUseCase;
        this.sessionStore = sessionStore;
        this.pageSize = pageSize;
    }

    @GetMapping(value = "/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liefert die im Java-Projekt eingebundenen ASTERIX-Beispiel-Dateien.")
    public List<AsterixSampleFile> listSamples() {
        return listAsterixSamplesUseCase.execute();
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Analysiert eine ASTERIX-Datei ueber den integrierten Java-Decoder.")
    public AsterixAnalysisResult analyze(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Bitte eine Datei hochladen.");
        }

        Path uploadFile = Files.createTempFile("asterix-upload-", ".ast");
        file.transferTo(uploadFile);

        AsterixAnalysisSession session = sessionStore.create(
                uploadFile,
                file.getOriginalFilename(),
                file.getSize()
        );

        return analyzeAsterixFileUseCase.execute(
                session.analysisId(),
                session.filePath(),
                session.fileName(),
                session.fileSizeBytes(),
                pageSize
        );
    }

    @PostMapping(value = "/samples/{sampleId}/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Analysiert eine eingebaute ASTERIX-Beispiel-Datei.")
    public AsterixAnalysisResult analyzeSample(@PathVariable String sampleId) {
        return analyzeAsterixSampleUseCase.execute(sampleId);
    }

    @GetMapping(value = "/analyses/{analysisId}/categories/{categoryKey}/records", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Liefert eine paginierte Record-Seite fuer eine Kategorie.")
    public AsterixCategoryPage getCategoryRecordsPage(
            @PathVariable String analysisId,
            @PathVariable String categoryKey,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AsterixAnalysisSession session = sessionStore.get(analysisId);
        return analyzeAsterixFileUseCase.loadCategoryPage(session.analysisId(), session.filePath(), categoryKey, page, size);
    }
}
