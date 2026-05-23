package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.PaymentMethod;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Validated
public class ConfirmCheckOutRequest {
    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Mã phiên gửi xe là bắt buộc")
    private UUID parkingSessionId;

    @NotNull(message = "Số tiền gửi xe là bắt buộc")
    private BigInteger parkingAmount;

    @NotNull(message = "Số tiền phạt là bắt buộc")
    private BigInteger penaltyAmount;

    @NotBlank(message = "URL hình ảnh ra xe là bắt buộc")
    private String imageOutUrl;

    private Float confidenceOut;

    @NotNull(message = "Làn ra xe là bắt buộc")
    private UUID exitLaneId;

    @NotNull(message = "Thời gian ra xe là bắt buộc")
    private LocalDateTime timeOut;

    private List<UUID> relatedSessionIds;
}