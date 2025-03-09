package sharespace.controller;

import sharespace.model.*;
import sharespace.payload.RoommateDTO;
import sharespace.payload.VacateResponseDTO;
import sharespace.service.RoommateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoommateControllerTest {

    @Mock
    private RoommateService roommateService;

    @InjectMocks
    private RoommateController roommateController;

    private RoommateDTO roommateDTO1;
    private RoommateDTO roommateDTO2;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roommateDTO1 = new RoommateDTO();
        roommateDTO1.setUsername("user1");
        roommateDTO2 = new RoommateDTO();
        roommateDTO2.setUsername("user2");
    }

    @Test
    void getAllRoommates() {

        List<RoommateDTO> roommateList = Arrays.asList(roommateDTO1, roommateDTO2);

        when(roommateService.getAllRoommates()).thenReturn(roommateList);

        ResponseEntity<List<RoommateDTO>> response = roommateController.getAllRoommates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(roommateService,times(1)).getAllRoommates();
    }

    @Test
    void updateRoommate() {
        int roommateId=1;
        Roommate roommate=new Roommate();
        roommate.setUsername("James");
        when(roommateService.updateRoommate(roommateId,roommate)).thenReturn("Roommate details was updated");

        ResponseEntity<String> updatedRoommate=roommateController.updateRoommate(roommateId,roommate);

        Assertions.assertEquals(HttpStatus.OK,updatedRoommate.getStatusCode());
        assertEquals("Roommate details was updated",updatedRoommate.getBody());
        verify(roommateService,times(1)).updateRoommate(roommateId,roommate);
    }

    @Test
    void updateEmail() {
        int id = 1;
        EmailUpdateRequest emailUpdateRequest = new EmailUpdateRequest();
        emailUpdateRequest.setEmail("newemail@example.com");

        Roommate updatedRoommate = new Roommate();
        updatedRoommate.setEmail("newemail@example.com");

        when(roommateService.updateEmail(id, emailUpdateRequest.getEmail())).thenReturn("Roommate details was updated");

        ResponseEntity<String> response = roommateController.updateEmail(id, emailUpdateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Roommate details was updated", response.getBody());
        verify(roommateService, times(1)).updateEmail(id, emailUpdateRequest.getEmail());
    }

    @Test
    void removeDetails() {
        String roommateName="Mike";

        ResponseEntity<String> response=roommateController.removeDetails(roommateName);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Roommate Detail have been removed Successfully",response.getBody());
        verify(roommateService,times(1)).deleteRoommate(roommateName);
    }

    @Test
    void getRoommate() {
        LoginDetails loginDetails=new LoginDetails("Admin","admin");
        Roommate roommate=new Roommate();
        roommate.setUsername("testuser");

        when(roommateService.getRoommate(loginDetails)).thenReturn(roommate);

        ResponseEntity<Roommate> response=roommateController.getRoommate(loginDetails);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("testuser",response.getBody().getUsername());
        verify(roommateService,times(1)).getRoommate(loginDetails);
    }

    @Test
    void updateDetails() {
        int id=1;
        UpdateDetails updateDetails=new UpdateDetails();
        updateDetails.setUsername("testuser");


        when(roommateService.updateDetails(id,updateDetails)).thenReturn(roommateDTO1);

        ResponseEntity<RoommateDTO> response=roommateController.updateDetails(id,updateDetails);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("user1",response.getBody().getUsername());
        verify(roommateService,times(1)).updateDetails(id,updateDetails);

    }

    @Test
    void sendVacateRequest() {
        VacateRequest vacateRequest=new VacateRequest();
        String message="Vacate Request sent";

        when(roommateService.sendVacateRequest(1,vacateRequest)).thenReturn(message);

        ResponseEntity<String> response=roommateController.sendVacateRequest(1,vacateRequest);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Vacate Request sent",response.getBody());
        verify(roommateService,times(1)).sendVacateRequest(1,vacateRequest);
    }

    @Test
    void getPendingRequests() {
        VacateResponseDTO vacateResponseDTO1=new VacateResponseDTO();
        VacateResponseDTO vacateResponseDTO2=new VacateResponseDTO();
        List<VacateResponseDTO> vacateResponseDTOS=Arrays.asList(vacateResponseDTO2,vacateResponseDTO1);

        when(roommateService.getPendingVacateRequests()).thenReturn(vacateResponseDTOS);

        ResponseEntity<List<VacateResponseDTO>> response=roommateController.getPendingRequests();

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(2,response.getBody().size());
        verify(roommateService,times(1)).getPendingVacateRequests();

    }

    @Test
    void markRequestAsRead() {
        ResponseEntity<Void> response=roommateController.markRequestAsRead(1);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        verify(roommateService,times(1)).markAsRead(1);
    }
}