package smartparkingsystem.backend.service.thirdService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    @Value("${file.upload-dir}")
    private String uploadRootPath;
    public Path getImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh cho phiên đỗ xe");
        }
        imageUrl = imageUrl.trim();
        if (imageUrl.startsWith("\"") && imageUrl.endsWith("\"")) {
            imageUrl = imageUrl.substring(1, imageUrl.length() - 1);
        }
        Path imagePath = Path.of(uploadRootPath, "images", imageUrl.replace("\\", "/")).toAbsolutePath().normalize();
        if (!Files.exists(imagePath)) {
            throw new ResourceNotFoundException("Không tìm thấy file ảnh: " + imageUrl);
        }

        return imagePath;
    }

    public float confidenceOrRandom(Float confidenceFromAi) {
        if (confidenceFromAi != null) {
            return confidenceFromAi;
        }
        return (float) (0.9 + Math.random() * 0.1);
    }

    public String buildAbsoluteImagePath(String relativeImageUrl) {
        if (relativeImageUrl == null || relativeImageUrl.isBlank()) {
            return null;
        }
        return Path.of(uploadRootPath, "images", relativeImageUrl).toString().replace("\\", "/");
    }
    public String storeImage(MultipartFile image, String folder, String failureMessage) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        try {
            // Tạo đường dẫn vật lý tuyệt đối: uploads/images/check-in/
            Path uploadDir = Path.of(uploadRootPath, "images", folder);
            Files.createDirectories(uploadDir);

            String extension = "";
            String originalFileName = image.getOriginalFilename();
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadDir.resolve(fileName);

            // Lưu file
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = folder + "/" + fileName;
            return relativePath.replace("\\", "/");

        } catch (IOException ex) {
            throw new IllegalStateException(failureMessage, ex);
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ValidationException("URL ảnh không được để trống");
        }
        imageUrl = imageUrl.trim();
        if (imageUrl.startsWith("\"") && imageUrl.endsWith("\"")) {
            imageUrl = imageUrl.substring(1, imageUrl.length() - 1);
        }

        String relativeImagePath = imageUrl.replace("\\", "/");
        Path baseDir = Path.of(uploadRootPath, "images").toAbsolutePath().normalize();
        Path imagePath = baseDir.resolve(relativeImagePath).normalize();
        if (!imagePath.startsWith(baseDir)) {
            throw new ValidationException("Đường dẫn ảnh không hợp lệ");
        }
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Không thể xóa file ảnh do lỗi hệ thống");
        }
    }
}
