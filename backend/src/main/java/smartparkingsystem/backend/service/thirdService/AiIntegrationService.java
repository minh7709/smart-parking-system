package smartparkingsystem.backend.service.thirdService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import smartparkingsystem.backend.exception.AiServiceException;

@Service
public class AiIntegrationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_SERVICE_URL = "/api/v1/ai/detect-plate";

    public String getLicensePlateFromAi(MultipartFile imageFile) {

        try {
            // Validate input
            if (imageFile == null || imageFile.isEmpty()) {
                throw new AiServiceException("Tập tin hình ảnh trống hoặc không hợp lệ");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "image.jpg";
                }
            };

            body.add("file", fileAsResource);

            // Đóng gói Header và Body vào HttpEntity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Bắn Request sang AI Service bằng method POST
            ResponseEntity<String> response = restTemplate.postForEntity(
                    AI_SERVICE_URL,
                    requestEntity,
                    String.class
            );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new AiServiceException("AI Service trả về phản hồi trống");
            }

            return response.getBody();

        } catch (AiServiceException e) {
            // Ném lỗi custom của chúng ta
            throw e;
        } catch (Exception e) {
            // Wrap các exception khác vào AiServiceException
            String errorMessage = "Lỗi khi xử lý hình ảnh: " + e.getMessage();
            throw new AiServiceException(errorMessage, e);
        }
    }
}