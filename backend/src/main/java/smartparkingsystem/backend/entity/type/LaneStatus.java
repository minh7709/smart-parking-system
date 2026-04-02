package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LaneStatus {
    ACTIVE ("Hoạt động"), MAINTENANCE ("Đang sửa"), DELETED ("Đã xóa");
    private final String label;
    }
