package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.dto.request.lane.LaneCreateRequest;
import smartparkingsystem.backend.dto.request.lane.LaneUpdateRequest;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.mapper.LaneMapper;
import smartparkingsystem.backend.repository.LaneRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLaneServiceTest {
    @Mock
    private LaneRepository laneRepository;

    @Mock
    private LaneMapper laneMapper;

    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    @InjectMocks
    private AdminLaneService adminLaneService;

    @Test
    void createLane_duplicateIpCamera_throwException() {
        // ip camera da ton tai
        LaneCreateRequest request = new LaneCreateRequest();
        request.setIpCamera("192.168.1.10");

        when(laneRepository.existsByIpCameraAndDeletedFalse("192.168.1.10")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminLaneService.createLane(request));
        verify(laneRepository, never()).save(any(Lane.class));
    }

    @Test
    void updateLane_ipCameraDuplicate_throwException() {
        UUID laneId = UUID.randomUUID();
        LaneUpdateRequest request = new LaneUpdateRequest();
        request.setLaneName("Lane A");
        request.setLaneType(LaneTypeEnum.IN);
        request.setStatus(LaneStatus.ACTIVE);
        request.setIpCamera("192.168.1.20");

        when(laneRepository.findByIdAndDeletedFalse(laneId)).thenReturn(Optional.of(new Lane()));
        when(laneRepository.existsByIpCameraAndDeletedFalseAndIdNot("192.168.1.20", laneId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminLaneService.updateLane(laneId, request));
    }

    @Test
    void deleteLane_softDelete() {
        UUID laneId = UUID.randomUUID();
        Lane lane = new Lane();
        lane.setDeleted(false);

        when(laneRepository.findByIdAndDeletedFalse(laneId)).thenReturn(Optional.of(lane));

        adminLaneService.deleteLane(laneId);

        ArgumentCaptor<Lane> captor = ArgumentCaptor.forClass(Lane.class);
        verify(laneRepository).save(captor.capture());
        assertEquals(true, captor.getValue().getDeleted());
    }

    @Test
    void getLaneById_notFound_throwException() {
        UUID laneId = UUID.randomUUID();
        when(laneRepository.findByIdAndDeletedFalse(laneId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminLaneService.getLaneById(laneId));
    }
}
