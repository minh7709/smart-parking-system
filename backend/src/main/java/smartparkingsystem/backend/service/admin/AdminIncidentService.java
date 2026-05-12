package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.dto.response.IncidentResponse;
import smartparkingsystem.backend.entity.Incident;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.IncidentMapper;
import smartparkingsystem.backend.repository.IncidentRepository;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AdminIncidentService {
    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;

    public Page<IncidentResponse> getIncidents(Pageable pageable, IncidentTypeEnum incidentTypeEnum) {
        Sort sort = Sort.by(Sort.Direction.DESC, "reportedAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Incident> page;
        if (incidentTypeEnum != null) {
            page = incidentRepository.findByIncidentType(incidentTypeEnum, sortedPageable);
        } else {
            page = incidentRepository.findAll(sortedPageable);
        }
        return page.map(incidentMapper::toResponse);
    }

    public Resource getEvidence(java.util.UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố với ID: " + incidentId));
        String evidencePath = incident.getEvidenceUrl();
        if(evidencePath == null || evidencePath.trim().isEmpty()) {
            throw new ResourceNotFoundException("Sự cố này không có file chứng cứ");
        }
        try {
            Path filePath = Paths.get(evidencePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Không tìm thấy hoặc không thể đọc file ảnh.");
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Đường dẫn file không hợp lệ.");
        }
    }
}
