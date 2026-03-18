package backend.presentation.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Systemstatus des Backends.")
public record SystemStatusResponse(
        @Schema(example = "Java-Backend verbunden")
        String title,
        @Schema(example = "Das React-Frontend laedt diese Daten ueber HTTP aus dem Backend.")
        String description,
        @Schema(example = "online")
        String level,
        @Schema(example = "2026-03-12T14:13:32.717Z")
        String updatedAt
) {
}
