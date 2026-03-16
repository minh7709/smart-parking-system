package smartparkingsystem.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // 2. Nếu chưa có, tạo tài khoản Admin mặc định
            User admin = new User();
            admin.setUsername("admin");

            // QUAN TRỌNG: Phải mã hóa mật khẩu trước khi lưu
            admin.setPassword(passwordEncoder.encode("12345678"));

            admin.setRole(UserRole.ADMIN);
            admin.setFullName("System Administrator");
            admin.setPhone("0123456789");
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
        }
    }
}