package sharespace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger= LoggerFactory.getLogger(RoommateController.class);

    public RoommateController(RoommateService roommateService) {
        this.roommateService = roommateService;
    }

    @GetMapping("/all-roommates")
    public ResponseEntity<List<Roommate>> getAllRoommates(){
        logger.info("Received an request to fetch all the Roommate details");
        List<Roommate> roommateList =roommateService.getAllRoommates();
        logger.info("Fetched {} roommate details",roommateList.size());
        return new ResponseEntity<>(roommateList, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Roommate> updateRoommate(@PathVariable int id, @RequestBody @Valid Roommate roommate) {
        logger.info("Received an request to update roommate details for roommate {}",id);
        Roommate updatedRoommate = roommateService.updateRoommate(id, roommate);
        logger.info("Roommate details updated for the {}",updatedRoommate.getUsername());
        return new ResponseEntity<>(updatedRoommate, HttpStatus.OK);
    }

    @PatchMapping("/email/{id}")
    public ResponseEntity<Roommate> updateEmail(@PathVariable int id, @RequestBody @Valid EmailUpdateRequest emailUpdateRequest) {
        Roommate updatedRoommate = roommateService.updateEmail(id, emailUpdateRequest.getEmail());
        logger.info("Email id was updated for the {}",updatedRoommate.getUsername());
        return new ResponseEntity<>(updatedRoommate, HttpStatus.OK);
    }

    @DeleteMapping("/vacate/{username}")
    public ResponseEntity<String> removeDetails(@PathVariable String username){
        logger.info("Requested to vacate {} from the room",username);
        roommateService.deleteRoommate(username);
        logger.info("{} have been removed Successfully",username);
        return new ResponseEntity<>("Roommate Detail have been removed Successfully",HttpStatus.OK);

    }

    @PostMapping("/get-roommate")
    public ResponseEntity<Roommate> getRoommate(@RequestBody LoginDetails loginDetails){
        logger.info("{} is trying to login",loginDetails.getUsername());
        Roommate roommate= roommateService.getRoommate(loginDetails);
        logger.info("{} was logged in successfully",loginDetails.getUsername());
        return new ResponseEntity<>(roommate,HttpStatus.OK);
    }

    @PatchMapping("/update-details/{roommateId}")
    public ResponseEntity<Roommate> updateDetails(@PathVariable int roommateId,@Valid @RequestBody UpdateDetails updateDetails){
        logger.info("Received an request to update the roommate details for the roommateId {}",roommateId);
        Roommate roommate=roommateService.updateDetails(roommateId,updateDetails);
        logger.info("Successfully updated details for the {}",roommate.getUsername());
        return new ResponseEntity<>(roommate,HttpStatus.OK);
    }

    @PostMapping("/send-vacate-request/{roommateId}")
    public ResponseEntity<String> sendVacateRequest(@PathVariable int roommateId, @RequestBody VacateRequest vacateRequest){
        logger.info("Roommate {} have sent the vacate request",roommateId);
        String response=roommateService.sendVacateRequest(roommateId,vacateRequest);
        logger.info("request have been submitted successfully");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/pending-vacate-request")
    public ResponseEntity<List<VacateResponseDTO>> getPendingRequests() {
        logger.info("Request for the pending payments list");
        List<VacateResponseDTO> vacateResponseDTOS = roommateService.getPendingVacateRequests();
        logger.info("Still {} yet to pay the rent",vacateResponseDTOS.size());
        return ResponseEntity.ok(vacateResponseDTOS);
    }

    @PutMapping("/mark-read/{requestId}")
    public ResponseEntity<Void> markRequestAsRead(@PathVariable int requestId) {
        logger.info("Received an request to mark the vacate request as read for {}",requestId);
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
        logger.info("Request to sort the Roommate details");
        Page<Roommate> roommates=roommateService.sortRoommates(page,limit,rentStatus,sortField,sortOrder);
        logger.info("Received {} roommate details",roommates.getTotalElements());
        return new ResponseEntity<>(roommates,HttpStatus.OK);
    }

}
