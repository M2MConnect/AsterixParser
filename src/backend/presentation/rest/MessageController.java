package backend.presentation.rest;

import backend.application.usecase.EchoMessageUseCase;
import backend.presentation.rest.dto.EchoMessageRequest;
import backend.presentation.rest.dto.EchoMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages")
public class MessageController {

    private final EchoMessageUseCase echoMessageUseCase;

    public MessageController(EchoMessageUseCase echoMessageUseCase) {
        this.echoMessageUseCase = echoMessageUseCase;
    }

    @PostMapping("/echo")
    @Operation(summary = "Sendet eine Nachricht ans Backend und erhaelt eine Echo-Antwort.")
    @ApiResponse(
            responseCode = "200",
            description = "Echo erfolgreich erzeugt.",
            content = @Content(schema = @Schema(implementation = EchoMessageResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Request ist ungueltig.")
    public EchoMessageResponse echo(@Valid @RequestBody EchoMessageRequest request) {
        return new EchoMessageResponse(echoMessageUseCase.execute(request.message()).value());
    }
}
