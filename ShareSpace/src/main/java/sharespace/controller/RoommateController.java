package sharespace.controller;

import org.springframework.data.domain.Page;
import sharespace.model.*;
import sharespace.payload.VacateResponseDTO;
import sharespace.service.RoommateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roommate")
public class RoommateController {


    private final RoommateService roommateService;

    public RoommateController(RoommateService roommateService) {
        this.roommateService = roommateService;
    }

    @GetMapping("/all-roommates")
    public ResponseEntity<List<Roommate>> getAllRoommates(){
        List<Roommate> roommateList =roommateService.getAllRoommates();
        return new ResponseEntity<>(roommateList, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Roommate> updateRoommate(@PathVariable int id, @RequestBody @Valid Roommate roommate) {
        Roommate updatedRoommate = roommateService.updateRoommate(id, roommate);
        return new ResponseEntity<>(updatedRoommate, HttpStatus.OK);
    }

    @PatchMapping("/email/{id}")
    public ResponseEntity<Roommate> updateEmail(@PathVariable int id, @RequestBody @Valid EmailUpdateRequest emailUpdateRequest) {
        Roommate updatedRoommate = roommateService.updateEmail(id, emailUpdateRequest.getEmail());
        return new ResponseEntity<>(updatedRoommate, HttpStatus.OK);
    }

    @DeleteMapping("/vacate/{username}")
    public ResponseEntity<String> removeDetails(@PathVariable String username){
        roommateService.deleteRoommate(username);
        return new ResponseEntity<>("Roommate Detail have been removed Successfully",HttpStatus.OK);

    }

    @PostMapping("/get-roommate")
    public ResponseEntity<Roommate> getRoommate(@RequestBody LoginDetails loginDetails){
        Roommate roommate= roommateService.getRoommate(loginDetails);

        return new ResponseEntity<>(roommate,HttpStatus.OK);
    }

    @PatchMapping("/update-details/{roommateId}")
    public ResponseEntity<Roommate> updateDetails(@PathVariable int roommateId,@RequestBody UpdateDetails updateDetails){
        Roommate roommate=roommateService.updateDetails(roommateId,updateDetails);

        return new ResponseEntity<>(roommate,HttpStatus.OK);
    }

    @PostMapping("/send-vacate-request/{roommateId}")
    public ResponseEntity<String> sendVacateRequest(@PathVariable int roommateId, @RequestBody VacateRequest vacateRequest){
        String response=roommateService.sendVacateRequest(roommateId,vacateRequest);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/pending-vacate-request")
    public ResponseEntity<List<VacateResponseDTO>> getPendingRequests() {
        List<VacateResponseDTO> vacateResponseDTOS = roommateService.getPendingVacateRequests();

        return ResponseEntity.ok(vacateResponseDTOS);
    }

    @PutMapping("/mark-read/{requestId}")
    public ResponseEntity<Void> markRequestAsRead(@PathVariable int requestId) {
        roommateService.markAsRead(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sort")
    public ResponseEntity<Page<Roommate>> sortRoommates(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "rentStatus",required = false) RentStatus rentStatus,
            @RequestParam(name = "sortField",defaultValue = "username" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder

    ){
        Page<Roommate> roommates=roommateService.sortRoommates(page,limit,rentStatus,sortField,sortOrder);
        return new ResponseEntity<>(roommates,HttpStatus.OK);
    }

}
