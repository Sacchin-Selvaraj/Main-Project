package sharespace.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sharespace.model.OwnerDetails;
import sharespace.service.OwnerService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerControllerTest {

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private OwnerController ownerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getOwnerDetails() {
        OwnerDetails ownerDetails=new OwnerDetails();
        ownerDetails.setOwnerName("testuser");

        when(ownerService.verifyOwnerDetails(ownerDetails)).thenReturn("Verified");

        ResponseEntity<String> response=ownerController.getOwnerDetails(ownerDetails);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Verified",response.getBody());
        verify(ownerService,times(1)).verifyOwnerDetails(ownerDetails);

    }
}