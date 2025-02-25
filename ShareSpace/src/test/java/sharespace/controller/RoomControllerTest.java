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
import sharespace.payload.RoommateDTO;
import sharespace.service.RoomService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getAllRoom() {
        Room room1=new Room();
        Room room2=new Room();
        List<Room> roomList= Arrays.asList(room1,room2);
        when(roomService.getAllRoomDetails()).thenReturn(roomList);

        ResponseEntity<List<Room>> response=roomController.getAllRoom();

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(2,response.getBody().size());
        verify(roomService,times(1)).getAllRoomDetails();

    }

    @Test
    void getRoom() {

        Room room = new Room();
        room.setRoomId(1);
        room.setRoomNumber("Room 101");

        when(roomService.getRoomById(1)).thenReturn(room);

        ResponseEntity<Room> response = roomController.getRoom(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Room 101", response.getBody().getRoomNumber());
        verify(roomService, times(1)).getRoomById(1);
    }

    @Test
    void CheckAvailability() {
        AvailabilityCheck availabilityCheck = new AvailabilityCheck();
        availabilityCheck.setCapacity(1);

        Room room1 = new Room();
        room1.setRoomId(1);
        room1.setRoomNumber("Room 101");

        Room room2 = new Room();
        room2.setRoomId(2);
        room2.setRoomNumber("Room 102");

        List<Room> availableRooms = Arrays.asList(room1, room2);

        when(roomService.checkAvailability(availabilityCheck)).thenReturn(availableRooms);

        ResponseEntity<List<Room>> response = roomController.checkAvailabilty(availabilityCheck);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Room 101", response.getBody().get(0).getRoomNumber());
        verify(roomService, times(1)).checkAvailability(availabilityCheck);
    }


    @Test
    void bookRoom() {

        int roomId = 1;
        Roommate roommate = new Roommate();
        roommate.setRoommateId(1);
        roommate.setUsername("user1");

        RoommateDTO roommateDTO = new RoommateDTO();
        roommateDTO.setRoommateId(1);
        roommateDTO.setUsername("user1");

        when(roomService.bookRoom(roomId, roommate)).thenReturn(roommateDTO);

        ResponseEntity<RoommateDTO> response = roomController.bookRoom(roomId, roommate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user1", response.getBody().getUsername());
        verify(roomService, times(1)).bookRoom(roomId, roommate);

    }
}