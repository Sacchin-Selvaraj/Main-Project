package sharespace.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.OwnerRoomDTO;
import sharespace.payload.RoomDTO;
import sharespace.payload.RoommateDTO;
import sharespace.service.RoomService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private RoomDTO roomDTO1;
    private RoomDTO roomDTO2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roomDTO1=new RoomDTO();
        roomDTO1.setRoomType("Single Sharing");
        roomDTO1.setCapacity(1);
        roomDTO1.setRoomNumber("F1");
        roomDTO1.setCurrentCapacity(0);
        roomDTO1.setFloorNumber(1);
        roomDTO1.setPrice(7000.00);
        roomDTO1.setPerDayPrice(250.00);
        roomDTO1.setIsAcAvailable(true);

        roomDTO2=new RoomDTO();
        roomDTO2.setRoomType("Two Sharing");
        roomDTO2.setCapacity(2);
        roomDTO2.setCurrentCapacity(0);
        roomDTO2.setFloorNumber(2);
        roomDTO2.setPrice(6000.00);
        roomDTO2.setPerDayPrice(230.00);
        roomDTO2.setIsAcAvailable(true);
    }


    @Test
    void getAllRoom() {
        OwnerRoomDTO ownerRoomDTO=new OwnerRoomDTO();
        List<OwnerRoomDTO> roomList= Collections.singletonList(ownerRoomDTO);
        when(roomService.getAllRoomDetails()).thenReturn(roomList);

        ResponseEntity<List<OwnerRoomDTO>> response=roomController.getAllRoom();

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().size());
        verify(roomService,times(1)).getAllRoomDetails();

    }

    @Test
    void getRoom() {
        when(roomService.getRoomById(1)).thenReturn(roomDTO1);

        ResponseEntity<RoomDTO> response = roomController.getRoom(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("F1", response.getBody().getRoomNumber());
        verify(roomService, times(1)).getRoomById(1);
    }

    @Test
    void CheckAvailability() {
        AvailabilityCheck availabilityCheck = new AvailabilityCheck();
        availabilityCheck.setCapacity(1);

        List<RoomDTO> roomDTOS=Arrays.asList(roomDTO1,roomDTO2);

        when(roomService.checkAvailability(availabilityCheck)).thenReturn(roomDTOS);

        ResponseEntity<List<RoomDTO>> response = roomController.checkAvailability(availabilityCheck);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("F1", response.getBody().get(0).getRoomNumber());
        verify(roomService, times(1)).checkAvailability(availabilityCheck);
    }


    @Test
    void bookRoom() {

        int roomId = 1;
        Roommate roommate = new Roommate();
        roommate.setRoommateId(1);
        roommate.setUsername("user1");
        String message="Room booked successfully for roommate ";

        when(roomService.bookRoom(roomId, roommate)).thenReturn(message);

        ResponseEntity<String> response = roomController.bookRoom(roomId, roommate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Room booked successfully for roommate ", response.getBody() );
        verify(roomService, times(1)).bookRoom(roomId, roommate);

    }
}