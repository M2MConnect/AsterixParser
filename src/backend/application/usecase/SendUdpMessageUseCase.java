package backend.application.usecase;

import backend.domain.model.Message;
import backend.domain.model.ServerResponse;
import backend.domain.ports.UdpClientPort;

public class SendUdpMessageUseCase {

    private final UdpClientPort udpClientPort;

    public SendUdpMessageUseCase(UdpClientPort udpClientPort) {
        this.udpClientPort = udpClientPort;
    }

    public ServerResponse execute(String host, int port, String rawMessage) {
        validatePort(port);
        return udpClientPort.send(host, port, new Message(rawMessage));
    }

    private void validatePort(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port muss zwischen 1 und 65535 liegen.");
        }
    }
}
