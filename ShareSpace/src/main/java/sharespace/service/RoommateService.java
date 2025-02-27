package sharespace.service;

import sharespace.model.LoginDetails;
import sharespace.model.Roommate;
import sharespace.model.UpdateDetails;
import sharespace.model.VacateRequest;
import sharespace.payload.VacateResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface RoommateService {

    List<Roommate> getAllRoommates();

    Roommate updateEmail(int id, String email);

    void deleteRoommate(String username);

    Roommate updateRoommate(int id, @Valid Roommate roommates);

    Roommate getRoommate(LoginDetails loginDetails);

    Roommate updateDetails(int roommateId, UpdateDetails updateDetails);

    String sendVacateRequest(int roommateId, VacateRequest vacateRequest);

    List<VacateResponseDTO> getPendingVacateRequests();

    void markAsRead(int requestId);

    List<Roommate> sortRoommates(Integer pageNumber, Integer limit,String rentStatus, String sortField, String sortOrder);
}
