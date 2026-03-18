package backend.infrastructure.config;

import backend.application.usecase.AnalyzeAsterixFileUseCase;
import backend.application.usecase.AnalyzeAsterixSampleUseCase;
import backend.application.usecase.EchoMessageUseCase;
import backend.application.usecase.GetSystemStatusUseCase;
import backend.application.usecase.ListAsterixSamplesUseCase;
import backend.domain.ports.AsterixDecoderPort;
import backend.infrastructure.storage.AsterixAnalysisSessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.AntPathMatcher;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public GetSystemStatusUseCase getSystemStatusUseCase() {
        return new GetSystemStatusUseCase();
    }

    @Bean
    public EchoMessageUseCase echoMessageUseCase() {
        return new EchoMessageUseCase();
    }

    @Bean
    public AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase(
            AsterixDecoderPort asterixDecoderPort,
            AsterixAnalysisSessionStore sessionStore,
            @Value("${asterix.decoder.preview-limit:3}") int previewLimit
    ) {
        return new AnalyzeAsterixFileUseCase(asterixDecoderPort, previewLimit, sessionStore);
    }

    @Bean
    public ListAsterixSamplesUseCase listAsterixSamplesUseCase() {
        return new ListAsterixSamplesUseCase();
    }

    @Bean
    public AnalyzeAsterixSampleUseCase analyzeAsterixSampleUseCase(
            AnalyzeAsterixFileUseCase analyzeAsterixFileUseCase,
            ListAsterixSamplesUseCase listAsterixSamplesUseCase,
            AsterixAnalysisSessionStore sessionStore,
            @Value("${asterix.decoder.preview-limit:10}") int previewLimit
    ) {
        return new AnalyzeAsterixSampleUseCase(
                analyzeAsterixFileUseCase,
                listAsterixSamplesUseCase,
                sessionStore,
                previewLimit
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                configurer.setPathMatcher(new AntPathMatcher());
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "OPTIONS");
            }
        };
    }
}
