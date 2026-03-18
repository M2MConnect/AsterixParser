package backend.infrastructure.storage;

import backend.domain.model.AsterixCategoryPage;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsterixAnalysisSessionStore {

    private final Map<String, AsterixAnalysisSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, AsterixCategoryPage> categoryPages = new ConcurrentHashMap<>();

    public AsterixAnalysisSession create(Path filePath, String fileName, long fileSizeBytes) {
        String analysisId = UUID.randomUUID().toString();
        AsterixAnalysisSession session = new AsterixAnalysisSession(analysisId, filePath, fileName, fileSizeBytes);
        sessions.put(analysisId, session);
        return session;
    }

    public AsterixAnalysisSession get(String analysisId) {
        AsterixAnalysisSession session = sessions.get(analysisId);
        if (session == null) {
            throw new IllegalArgumentException("Analyse-Session nicht gefunden: " + analysisId);
        }

        return session;
    }

    public AsterixCategoryPage getCategoryPage(String analysisId, String categoryKey, int page, int size) {
        return categoryPages.get(buildPageKey(analysisId, categoryKey, page, size));
    }

    public void putCategoryPage(String analysisId, String categoryKey, int page, int size, AsterixCategoryPage categoryPage) {
        categoryPages.put(buildPageKey(analysisId, categoryKey, page, size), categoryPage);
    }

    private String buildPageKey(String analysisId, String categoryKey, int page, int size) {
        return String.join(":", analysisId, categoryKey, Integer.toString(page), Integer.toString(size));
    }
}
