package smartparkingsystem.backend.mapper;

import smartparkingsystem.backend.dto.request.VehicleRequest;
import smartparkingsystem.backend.dto.response.VehicleReponse;
import smartparkingsystem.backend.entity.Vehicle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class VehiceMapper {

    private VehiceMapper() {
        // utility
    }

    /**
     * Map a create request to a new Vehicle entity.
     * Sets createdAt to now and isDeleted to false.
     */
    public static Vehicle toEntity(VehicleRequest req) {
        if (req == null) return null;
        Vehicle v = new Vehicle();
        v.setLicensePlate(req.getLicensePlate());
        v.setVehicleType(req.getVehicleType());
        v.setBrand(req.getBrand());
        v.setCustomerName(req.getCustomerName());
        v.setCustomerPhone(req.getCustomerPhone());
        v.setDeleted(false);
        v.setCreatedAt(LocalDateTime.now());
        return v;
    }

    /**
     * Update an existing Vehicle entity from a request (for PUT semantics).
     * Does not change id or createdAt. Sets updatedAt to now.
     */
    public static void updateEntityFromRequest(VehicleRequest req, Vehicle target) {
        if (req == null || target == null) return;
        // Only update allowed fields
        if (!Objects.equals(target.getLicensePlate(), req.getLicensePlate())) {
            target.setLicensePlate(req.getLicensePlate());
        }
        target.setVehicleType(req.getVehicleType());
        target.setBrand(req.getBrand());
        target.setCustomerName(req.getCustomerName());
        target.setCustomerPhone(req.getCustomerPhone());
        target.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Map entity to response DTO used by API clients.
     */
    public static VehicleReponse toResponse(Vehicle v) {
        if (v == null) return null;
        return VehicleReponse.builder()
                .id(v.getId())
                .licensePlate(v.getLicensePlate())
                .vehicleType(v.getVehicleType())
                .brand(v.getBrand())
                .customerName(v.getCustomerName())
                .customerPhone(v.getCustomerPhone())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    public static List<VehicleReponse> toResponseList(List<Vehicle> list) {
        if (list == null) return null;
        return list.stream().map(VehiceMapper::toResponse).collect(Collectors.toList());
    }

}
