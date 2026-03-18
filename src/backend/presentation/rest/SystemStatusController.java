package backend.presentation.rest;

import backend.application.usecase.GetSystemStatusUseCase;
import backend.domain.model.SystemStatus;
import backend.presentation.rest.dto.SystemStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
@Tag(name = "System Status")
public class SystemStatusController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;

    public SystemStatusController(GetSystemStatusUseCase getSystemStatusUseCase) {
        this.getSystemStatusUseCase = getSystemStatusUseCase;
    }

    @GetMapping
    @Operation(summary = "Liefert den aktuellen Backend-Status.")
    public SystemStatusResponse getStatus() {
        SystemStatus status = getSystemStatusUseCase.execute();
        return new SystemStatusResponse(
                status.title(),
                status.description(),
                status.level(),
                status.updatedAt()
        );
    }
}
