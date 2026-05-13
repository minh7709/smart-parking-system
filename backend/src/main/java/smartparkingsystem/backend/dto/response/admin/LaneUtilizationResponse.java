package smartparkingsystem.backend.dto.response.admin;

public interface LaneUtilizationResponse {
    String getLaneName();
    long getEntryCount();
    long getExitCount();
}
