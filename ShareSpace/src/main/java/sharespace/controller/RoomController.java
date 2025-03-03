package sharespace.controller;


import lombok.extern.slf4j.Slf4j;
import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.RoommateDTO;
import sharespace.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/room")
public class RoomController {


    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<Room>> getAllRoom(){
        List<Room> roomList=roomService.getAllRoomDetails();
        return new ResponseEntity<>(roomList, HttpStatus.OK);
    }

    @GetMapping("/get-room/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable int roomId ){
        Room room=roomService.getRoomById(roomId);
        return new ResponseEntity<>(room,HttpStatus.OK);
    }

    @PostMapping("/check-availability")
    public ResponseEntity<List<Room>> checkAvailability (@RequestBody AvailabilityCheck available){
        List<Room> room=roomService.checkAvailability(available);
        return new ResponseEntity<>(room,HttpStatus.OK);
    }

    @PostMapping("/book/{roomId}")
    public ResponseEntity<RoommateDTO> bookRoom(@PathVariable int roomId, @Valid @RequestBody Roommate roommate){
        RoommateDTO roommateDTO=roomService.bookRoom(roomId, roommate);
        return new ResponseEntity<>(roommateDTO,HttpStatus.OK);
    }

    @PostMapping("/add-room")
    public ResponseEntity<String> addRooms(@RequestBody Room room){
        log.info("Adding Room into DB");
        String response=roomService.addRooms(room);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/edit-room/{roomId}")
    public ResponseEntity<Room> editRoom(@PathVariable int roomId,@RequestBody Room room){
        Room room1=roomService.editRoom(roomId,room);
        return new ResponseEntity<>(room1,HttpStatus.OK);
    }

    @DeleteMapping("/delete-room/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable int roomId){
        String message=roomService.deleteRoom(roomId);
        return new ResponseEntity<>(message,HttpStatus.OK);
    }


}
