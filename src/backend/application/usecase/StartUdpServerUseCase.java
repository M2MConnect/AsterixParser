package backend.application.usecase;

import backend.domain.ports.UdpServerPort;

public class StartUdpServerUseCase {

    private final UdpServerPort udpServerPort;

    public StartUdpServerUseCase(UdpServerPort udpServerPort) {
        this.udpServerPort = udpServerPort;
    }

    public void execute(int port) {
        validatePort(port);
        udpServerPort.start(port);
    }

    private void validatePort(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port muss zwischen 1 und 65535 liegen.");
        }
    }
}
