package backend.domain.model;

public record Message(String value) {

    public Message {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Nachricht darf nicht leer sein.");
        }
    }
}
