package backend.domain.model;

import java.util.List;

public record AsterixRecordAnalysis(
        int index,
        int length,
        String fspec,
        int remainingBytes,
        String itemList,
        List<Integer> rawBytes,
        List<AsterixFspecOctet> fspecOctets,
        List<AsterixUapItem> uapItems
) {
}
