package backend.application.usecase;

import backend.domain.model.SystemStatus;

import java.time.Instant;

public class GetSystemStatusUseCase {

    public SystemStatus execute() {
        return new SystemStatus(
                "Java-Backend verbunden",
                "Das React-Frontend laedt diese Daten ueber HTTP aus dem Backend.",
                "online",
                Instant.now().toString()
        );
    }
}
