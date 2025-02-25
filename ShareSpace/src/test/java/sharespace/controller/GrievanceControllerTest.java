package sharespace.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sharespace.model.Grievances;
import sharespace.payload.GrievancesDTO;
import sharespace.service.GrievanceService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GrievanceControllerTest {

    @Mock
    private GrievanceService grievanceService;

    @InjectMocks
    private GrievanceController grievanceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRaiseAnGrievance() {
        int roommateId = 1;
        Grievances grievances = new Grievances();
        grievances.setGrievanceContent("Issue with AC");

        when(grievanceService.raiseAnGrievance(roommateId, grievances)).thenReturn("Grievance raised successfully");

        ResponseEntity<String> response = grievanceController.raiseAnGrievance(roommateId, grievances);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grievance raised successfully", response.getBody());
        verify(grievanceService, times(1)).raiseAnGrievance(roommateId, grievances);
    }

    @Test
    void testShowPendingGrievances() {
        GrievancesDTO grievance1 = new GrievancesDTO();
        grievance1.setGrievanceId(1);
        grievance1.setGrievanceContent("Issue with AC");

        GrievancesDTO grievance2 = new GrievancesDTO();
        grievance2.setGrievanceId(2);
        grievance2.setGrievanceContent("Broken Chair");

        List<GrievancesDTO> grievancesDTOS = Arrays.asList(grievance1, grievance2);

        when(grievanceService.getPendingGrievances()).thenReturn(grievancesDTOS);

        ResponseEntity<List<GrievancesDTO>> response = grievanceController.showPendingGrievances();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Issue with AC", response.getBody().get(0).getGrievanceContent());
        assertEquals("Broken Chair", response.getBody().get(1).getGrievanceContent());
        verify(grievanceService, times(1)).getPendingGrievances();
    }

    @Test
    void testMarkAsRead() {
        int grievanceId = 1;

        when(grievanceService.markPendingGrievances(grievanceId)).thenReturn("Grievance marked as read");

        ResponseEntity<String> response = grievanceController.markAsRead(grievanceId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grievance marked as read", response.getBody());
        verify(grievanceService, times(1)).markPendingGrievances(grievanceId);
    }
}