package sharespace.controller;

import sharespace.model.OwnerDetails;
import sharespace.service.OwnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
public class OwnerController {


    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> getOwnerDetails(@RequestBody OwnerDetails ownerDetails){
        String status= ownerService.verifyOwnerDetails(ownerDetails);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
