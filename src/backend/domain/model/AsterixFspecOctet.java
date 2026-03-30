package backend.domain.model;

public record AsterixFspecOctet(
        int octetIndex,
        String hexValue,
        String binaryValue,
        int fxValue,
        String definition
) {
}
