package backend.domain.ports;

import backend.domain.model.Message;
import backend.domain.model.ServerResponse;

public interface UdpClientPort {
    ServerResponse send(String host, int port, Message message);
}
