package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Data
@Validated
public class CheckOutRequest {
    @NotNull(message = "Mã làn ra là bắt buộc")
    private UUID exitLaneId;

    @NotNull(message = "Phiên gửi xe là bắt buộc")
    private UUID parkingSessionId;
}

