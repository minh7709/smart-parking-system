package smartparkingsystem.backend.service.guard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.type.LaneStatus;
import smartparkingsystem.backend.mapper.LaneMapper;
import smartparkingsystem.backend.repository.LaneRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {
    @Mock
    private LaneRepository laneRepository;

    @Mock
    private LaneMapper laneMapper;

    @InjectMocks
    private LaneService laneService;

    @Test
    void getActiveLanes_onlyActive() {
        when(laneRepository.findAllByStatusAndDeletedFalse(LaneStatus.ACTIVE))
                .thenReturn(List.of(new Lane()));

        laneService.getActiveLanes();

        verify(laneRepository).findAllByStatusAndDeletedFalse(LaneStatus.ACTIVE);
    }
}
