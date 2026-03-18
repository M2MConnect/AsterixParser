package backend.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request fuer eine Echo-Nachricht.")
public record EchoMessageRequest(
        @NotBlank
        @Schema(example = "Hallo Backend")
        String message
) {
}
