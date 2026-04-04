package smartparkingsystem.backend.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CASH ("Tiền mặt"), ONLINE_PAYMENT ("Chuyển khoản");
    private final String label;
    }
