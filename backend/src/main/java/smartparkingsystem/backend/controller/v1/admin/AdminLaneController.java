package smartparkingsystem.backend.controller.v1.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/lanes")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminLaneController {
}
