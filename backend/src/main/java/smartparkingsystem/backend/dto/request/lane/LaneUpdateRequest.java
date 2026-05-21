package smartparkingsystem.backend.dto.request.lane;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;

@Data
public class LaneUpdateRequest {
	@NotBlank(message = "Tên làn không được để trống")
	@Size(max = 50, message = "Tên làn tối đa 50 ký tự")
	private String laneName;

	@NotNull(message = "Loại làn là bắt buộc")
	private LaneTypeEnum laneType;

	@NotNull(message = "Trạng thái là bắt buộc")
	private LaneStatus status;

	@NotBlank(message = "IP camera không được để trống")
	@Size(max = 100, message = "IP camera tối đa 100 ký tự")
	private String ipCamera;
}
