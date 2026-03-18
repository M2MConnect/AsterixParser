package backend.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Antwort des Backends auf eine Echo-Nachricht.")
public record EchoMessageResponse(
        @Schema(example = "Server ACK: Hallo Backend")
        String message
) {
}
