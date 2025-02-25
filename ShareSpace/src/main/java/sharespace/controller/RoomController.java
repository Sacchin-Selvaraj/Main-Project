package sharespace.controller;


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

@RestController
@CrossOrigin
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
    public ResponseEntity<List<Room>> checkAvailabilty(@RequestBody AvailabilityCheck available){
        List<Room> room=roomService.checkAvailability(available);
        return new ResponseEntity<>(room,HttpStatus.OK);
    }

    @PostMapping("/book/{roomId}")
    public ResponseEntity<RoommateDTO> bookRoom(@PathVariable int roomId, @Valid @RequestBody Roommate roommate){
        RoommateDTO roommateDTO=roomService.bookRoom(roomId, roommate);
        return new ResponseEntity<>(roommateDTO,HttpStatus.OK);
    }


}
