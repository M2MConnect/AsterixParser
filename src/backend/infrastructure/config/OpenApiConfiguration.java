package backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI cleanArchitectureOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TestJava1 Backend API")
                        .version("v1")
                        .description("Spring Boot API fuer das React-Frontend in Clean Architecture.")
                        .contact(new Contact().name("Martin Mueller")))
                .addServersItem(new Server().url("http://localhost:8080"));
    }
}
