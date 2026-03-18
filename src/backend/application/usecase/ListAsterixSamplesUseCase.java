package backend.application.usecase;

import backend.domain.model.AsterixSampleFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListAsterixSamplesUseCase {

    private static final String SAMPLE_PATH_PATTERN = "classpath*:samples/*";

    public List<AsterixSampleFile> execute() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            Resource[] resources = resolver.getResources(SAMPLE_PATH_PATTERN);
            List<AsterixSampleFile> samples = new ArrayList<>();
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName == null || fileName.isBlank()) {
                    continue;
                }
                if (!isSupportedSample(fileName)) {
                    continue;
                }

                samples.add(new AsterixSampleFile(
                        buildId(fileName),
                        fileName,
                        resolveSize(resource),
                        describe(fileName)
                ));
            }

            samples.sort(Comparator.comparing(AsterixSampleFile::fileName));
            return samples;
        } catch (IOException exception) {
            throw new IllegalStateException("Beispiel-Dateien konnten nicht geladen werden.", exception);
        }
    }

    private long resolveSize(Resource resource) {
        try {
            return resource.contentLength();
        } catch (IOException ignored) {
            return -1L;
        }
    }

    private String buildId(String fileName) {
        return fileName.replace('.', '-').toLowerCase();
    }

    private String describe(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".bin")) {
            return "JASTERIX Binaerbeispiel fuer definierte Kategorien und Editionen.";
        }
        if (lower.contains("smr_mlat_adsb")) {
            return "Kombinierte Sample-Datei mit SMR, MLAT und ADS-B.";
        }
        if (lower.contains("adsb")) {
            return "ADS-B Beispiel-Datei.";
        }
        if (lower.contains("mlat")) {
            return "MLAT Beispiel-Datei.";
        }
        if (lower.contains("smr")) {
            return "SMR Beispiel-Datei.";
        }

        return "ASTERIX Beispiel-Datei.";
    }

    private boolean isSupportedSample(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".ast") || lower.endsWith(".bin");
    }
}
