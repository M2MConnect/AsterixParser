package backend.application.usecase;

import backend.domain.model.Message;
import backend.domain.model.ServerResponse;

public class EchoMessageUseCase {

    public ServerResponse execute(String rawMessage) {
        Message message = new Message(rawMessage);
        return new ServerResponse("Server ACK: " + message.value());
    }
}
