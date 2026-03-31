package backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI cleanArchitectureOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AsterixParser Backend API")
                        .version("v1")
                        .description("Spring Boot API fuer ASTERIX-Analyse, Sample-Dateien und Systemstatus.")
                        .contact(new Contact()
                                .name("AsterixParser Team")
                                .url("http://localhost:8080/swagger-ui.html"))
                        .license(new License()
                                .name("Internal Use")
                                .url("http://localhost:8080/api-docs")))
                .addServersItem(new Server().url("http://localhost:8080"));
    }
}
