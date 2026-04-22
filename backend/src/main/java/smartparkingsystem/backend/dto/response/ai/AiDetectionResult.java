package smartparkingsystem.backend.dto.response.ai;

import java.util.List;

public record AiDetectionResult(boolean success, String message, List<AiPlateCandidate> candidates) {

    public String getPlateNumber() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0).plateNumber();
    }

    public Float getConfidence() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0).confidence();
    }
}

