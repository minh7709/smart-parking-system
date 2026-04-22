package smartparkingsystem.backend.service.thirdService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import smartparkingsystem.backend.dto.response.ai.AiDetectionResult;
import smartparkingsystem.backend.dto.response.ai.AiPlateCandidate;
import smartparkingsystem.backend.exception.AiServiceException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiIntegrationService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;
    private final ObjectMapper objectMapper;

    public AiIntegrationService(@Value("${ai.service.url}") String aiServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.aiServiceUrl = aiServiceUrl;
        this.objectMapper = new ObjectMapper();
    }

    public AiDetectionResult getDetectionResultFromAi(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new AiServiceException("Tập tin hình ảnh trống hoặc không hợp lệ");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Send path in JSON so AI service can read image from absolute path.
            Map<String, String> body = new HashMap<>();
            body.put("imageUrl", imageUrl);
            body.put("imagePath", imageUrl);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(aiServiceUrl, requestEntity, String.class);

            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new AiServiceException("AI Service trả về phản hồi trống");
            }

            return parseAiResponse(response.getBody());

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Lỗi khi xử lý hình ảnh: " + e.getMessage();
            throw new AiServiceException(errorMessage, e);
        }
    }

    // Backward-compatible helper for old callers.
    public String getLicensePlateFromAi(String imageUrl) {
        return getDetectionResultFromAi(imageUrl).getPlateNumber();
    }

    private AiDetectionResult parseAiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            boolean success = root.path("success").asBoolean(false);
            String message = root.path("message").asText(null);

            if (!success) {
                throw new AiServiceException(message != null ? message : "AI service không nhận diện được biển số");
            }

            List<AiPlateCandidate> candidates = extractCandidates(root);
            if (candidates.isEmpty()) {
                throw new AiServiceException("AI service không trả về biển số hợp lệ");
            }

            candidates.sort(Comparator.comparing(
                    c -> c.confidence() == null ? Float.NEGATIVE_INFINITY : c.confidence(),
                    Comparator.reverseOrder()
            ));

            return new AiDetectionResult(true, message, candidates);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Không parse được phản hồi từ AI service", e);
        }
    }

    private List<AiPlateCandidate> extractCandidates(JsonNode root) {
        List<AiPlateCandidate> candidates = new ArrayList<>();

        addCandidateFromNode(candidates, root);

        JsonNode candidatesNode = root.path("candidates");
        if (candidatesNode.isArray()) {
            for (JsonNode node : candidatesNode) {
                addCandidateFromNode(candidates, node);
            }
        }

        JsonNode resultsNode = root.path("results");
        if (resultsNode.isArray()) {
            for (JsonNode node : resultsNode) {
                addCandidateFromNode(candidates, node);
            }
        }

        return candidates;
    }

    private void addCandidateFromNode(List<AiPlateCandidate> candidates, JsonNode node) {
        String plateNumber = textOrNull(node, "plateNumber");
        if (plateNumber == null) {
            plateNumber = textOrNull(node, "plate");
        }

        if (plateNumber == null || plateNumber.isBlank()) {
            return;
        }

        Float confidence = floatOrNull(node, "confidence");
        candidates.add(new AiPlateCandidate(plateNumber.trim(), confidence));
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText(null);
        return text == null || text.isBlank() ? null : text;
    }

    private Float floatOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.floatValue();
        }
        try {
            String text = value.asText(null);
            if (text == null || text.isBlank()) {
                return null;
            }
            return Float.parseFloat(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}