package sharespace.service;

import org.springframework.data.domain.Page;
import sharespace.model.*;
import sharespace.payload.RoommateDTO;
import sharespace.payload.VacateResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface RoommateService {

    List<RoommateDTO> getAllRoommates();

    String  updateEmail(int id, String email);

    void deleteRoommate(String username);

    String updateRoommate(int id, @Valid Roommate roommates);

    Roommate getRoommate(LoginDetails loginDetails);

    RoommateDTO updateDetails(int roommateId, UpdateDetails updateDetails);

    String sendVacateRequest(int roommateId, VacateRequest vacateRequest);

    List<VacateResponseDTO> getPendingVacateRequests();

    void markAsRead(int requestId);

    Page<RoommateDTO> sortRoommates(Integer pageNumber, Integer limit, RentStatus rentStatus, String sortField, String sortOrder);
}
