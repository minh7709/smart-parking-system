package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.VehicleRequest;
import smartparkingsystem.backend.dto.response.VehicleReponse;
import smartparkingsystem.backend.entity.Vehicle;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehicleRequest request){
        if(request == null){
            return null;
        }
        return Vehicle.builder()
                .licensePlate(request.getLicensePlate())
                .vehicleType(request.getVehicleType())
                .brand(request.getBrand())
                .customerName(request.getCustomerName())
                .deleted(false)
                .build();
    }

    public VehicleReponse toResponse(Vehicle vehicle){
        if(vehicle == null){
            return null;
        }
        return VehicleReponse.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .customerName(vehicle.getCustomerName())
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(vehicle.getVehicleType())
                .customerPhone(vehicle.getCustomerPhone())
                .deleted(vehicle.isDeleted())
                .build();
    }

    public void updateEntity(VehicleRequest request, Vehicle vehicle){
        if(request == null) {
            return;
        }
        vehicle.setBrand(request.getBrand());
        vehicle.setCustomerName(request.getCustomerName());
        vehicle.setCustomerPhone(request.getCustomerPhone());
    }
}
