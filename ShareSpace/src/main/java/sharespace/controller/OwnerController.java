package sharespace.controller;

import lombok.extern.slf4j.Slf4j;
import sharespace.model.OwnerDetails;
import sharespace.service.OwnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/owner")
public class OwnerController {


    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> getOwnerDetails(@RequestBody OwnerDetails ownerDetails){
        log.info("{} is trying to access the Owner dashboard",ownerDetails.getOwnerName());
        String status= ownerService.verifyOwnerDetails(ownerDetails);
        log.info("{}",status);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
