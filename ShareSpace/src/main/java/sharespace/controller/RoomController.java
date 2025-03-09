package sharespace.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.OwnerRoomDTO;
import sharespace.payload.RoomDTO;
import sharespace.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<OwnerRoomDTO>> getAllRoom(){
        log.info("Fetching all rooms");
        List<OwnerRoomDTO> roomList=roomService.getAllRoomDetails();
        log.info("Fetched {} rooms", roomList.size());
        return new ResponseEntity<>(roomList, HttpStatus.OK);
    }

    @GetMapping("/get-room/{roomId}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable int roomId ){
        log.info("Fetching room with ID: {}", roomId);
        RoomDTO roomDTO=roomService.getRoomById(roomId);
        if (roomDTO != null) {
            log.info("Room found with ID: {}", roomId);
        } else {
            log.warn("Room not found with ID: {}", roomId);
        }
        return new ResponseEntity<>(roomDTO,HttpStatus.OK);
    }

    @PostMapping("/check-availability")
    public ResponseEntity<List<RoomDTO>> checkAvailability (@RequestBody AvailabilityCheck available){
        log.info("Checking availability");
        List<RoomDTO> rooms = roomService.checkAvailability(available);
        log.info("Found {} available rooms", rooms.size());
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @PostMapping("/book/{roomId}")
    public ResponseEntity<String> bookRoom(@PathVariable int roomId, @Valid @RequestBody Roommate roommate){
        log.info("Booking room with Id: {}", roomId);
        String response=roomService.bookRoom(roomId, roommate);
        log.info("Room booked successfully with ID: {}", roomId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/add-room")
    public ResponseEntity<String> addRooms(@RequestBody Room room){
        log.info("Adding Room into DB");
        String response=roomService.addRooms(room);
        log.info("Room added with response: {}", response);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/edit-room/{roomId}")
    public ResponseEntity<RoomDTO> editRoom(@PathVariable int roomId,@RequestBody Room room){
        log.info("Editing room with ID: {}", roomId);
        RoomDTO updatedRoom=roomService.editRoom(roomId,room);
        if (updatedRoom != null) {
            log.info("Room updated successfully with ID: {}", roomId);
        } else {
            log.error("Failed to update room with ID: {}", roomId);
        }
        return new ResponseEntity<>(updatedRoom,HttpStatus.OK);
    }

    @DeleteMapping("/delete-room/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable int roomId){
        log.info("Deleting room with ID: {}", roomId);
        String message=roomService.deleteRoom(roomId);
        log.info("Room deleted successfully with ID: {}", roomId);
        return new ResponseEntity<>(message,HttpStatus.OK);
    }


}
