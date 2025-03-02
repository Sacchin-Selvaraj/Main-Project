package sharespace.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sharespace.model.Grievances;
import sharespace.payload.GrievancesDTO;
import sharespace.service.GrievanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/grievance")
public class GrievanceController {

    private final GrievanceService grievanceService;

    public GrievanceController(GrievanceService grievanceService) {
        this.grievanceService = grievanceService;
    }

    private static final Logger logger = LoggerFactory.getLogger(GrievanceController.class);

    /* @PostMapping("/raise/{roommateId}")
     public ResponseEntity<String> raiseAnGrievance(@PathVariable int roommateId, @RequestBody Grievances grievances) {
         logger.info("Received request to raise a grievance for roommateId: {}", roommateId);

             String response = grievanceService.raiseAnGrievance(roommateId, grievances);
             logger.info("Grievance raised successfully for roommateId: {}", roommateId);
             return new ResponseEntity<>(response, HttpStatus.OK);

     }

     @GetMapping("/pending-grievance")
     public ResponseEntity<List<GrievancesDTO>> showPendingGrievances(){
         List<GrievancesDTO> grievancesDTOS=grievanceService.getPendingGrievances();
         return ResponseEntity.ok(grievancesDTOS);
     }

     @PostMapping("/mark-as-read/{grievanceId}")
     public ResponseEntity<String> markAsRead(@PathVariable int grievanceId ){
         String response=grievanceService.markPendingGrievances(grievanceId);
         return ResponseEntity.ok(response);
     }
 */
    @PostMapping("/raise/{roommateId}")
    public ResponseEntity<String> raiseAnGrievance(@PathVariable int roommateId, @RequestBody Grievances grievances) {
        logger.info("Received request to raise a grievance for roommateId: {}", roommateId);
        try {
            String response = grievanceService.raiseAnGrievance(roommateId, grievances);
            logger.info("Grievance raised successfully for roommateId: {}", roommateId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while raising grievance for roommateId: {}", roommateId, e);
            return new ResponseEntity<>("Failed to raise grievance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pending-grievance")
    public ResponseEntity<List<GrievancesDTO>> showPendingGrievances() {
        logger.info("Received request to fetch pending grievances");
        try {
            List<GrievancesDTO> grievancesDTOS = grievanceService.getPendingGrievances();
            logger.info("Fetched {} pending grievances", grievancesDTOS.size());
            return ResponseEntity.ok(grievancesDTOS);
        } catch (Exception e) {
            logger.error("Error while fetching pending grievances", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mark-as-read/{grievanceId}")
    public ResponseEntity<String> markAsRead(@PathVariable int grievanceId) {
        logger.info("Received request to mark grievance as read for grievanceId: {}", grievanceId);
        try {
            String response = grievanceService.markPendingGrievances(grievanceId);
            logger.info("Grievance marked as read for grievanceId: {}", grievanceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while marking grievance as read for grievanceId: {}", grievanceId, e);
            return new ResponseEntity<>("Failed to mark grievance as read", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
