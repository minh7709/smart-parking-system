package smartparkingsystem.backend.entity.type;

@Getter
@RequiredArgsConstructor
public enum InvoiceTypeEnum {
    PARKING_FEE ('Phiên gửi xe'),
    SUBSCRIPTION_FEE ('Gói đăng ký'),
    private final String label;
}