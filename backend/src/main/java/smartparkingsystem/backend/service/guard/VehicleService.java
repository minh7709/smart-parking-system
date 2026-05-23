package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.VehicleRequest;
import smartparkingsystem.backend.dto.response.VehicleReponse;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.VehicleMapper;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final SubscriptionRepository subscriptionRepository;
    @Transactional
    public VehicleReponse createVehicle(VehicleRequest request){
        if(vehicleRepository.existsByLicensePlateAndDeletedFalse(request.getLicensePlate())){
            throw new DuplicateResourceException("Đã tồn tại phương tiện này");
        }
        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional
    public VehicleReponse updateVehicle(VehicleRequest request, UUID id){
        Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện để cập nhật"));
        vehicleMapper.updateEntity(request, vehicle);
        vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(vehicle);
    }

    @Transactional
    public VehicleReponse deleteVehicle(UUID id){
        Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện để cập nhật"));
        subscriptionRepository.findByVehicleIdAndStatusIn(id, List.of(SubStatus.PENDING, SubStatus.ACTIVE))
                .ifPresent(sub -> {
                    throw new InvalidStateException("Không thể xóa phương tiện đang có đăng ký hoạt động hoặc chờ duyệt");
                });
        vehicle.setDeleted(true);
        vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(vehicle);
    }
    @Transactional(readOnly = true)
    public Page<VehicleReponse> getVehicles(Pageable pageable, String licensePlate, VehicleTypeEnum vehicleType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Vehicle> page;

        boolean hasLicensePlate = licensePlate != null && !licensePlate.isBlank();
        boolean hasVehicleType = vehicleType != null;

        if (hasLicensePlate && hasVehicleType) {
            page = vehicleRepository.findByLicensePlateContainingIgnoreCaseAndVehicleTypeAndDeletedFalse(
                    licensePlate,
                    vehicleType,
                    sortedPageable
            );
        } else if (hasLicensePlate) {
            page = vehicleRepository.findByLicensePlateContainingIgnoreCaseAndDeletedFalse(
                    licensePlate,
                    sortedPageable
            );
        } else if (hasVehicleType) {
            page = vehicleRepository.findByVehicleTypeAndDeletedFalse(vehicleType, sortedPageable);
        } else {
            page = vehicleRepository.findAllByDeletedFalse(sortedPageable);
        }
        return page.map(vehicleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public VehicleReponse getVehicleById(UUID id){
        Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện"));
        return vehicleMapper.toResponse(vehicle);
    }
}
