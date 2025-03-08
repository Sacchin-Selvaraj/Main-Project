package sharespace.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sharespace.exception.OwnerException;
import sharespace.model.OwnerDetails;
import sharespace.password.PasswordUtils;
import sharespace.repository.OwnerRepository;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger log = LoggerFactory.getLogger(OwnerServiceImpl.class);

    private final OwnerRepository ownerRepo;
    private final PasswordUtils passwordUtils;

    public OwnerServiceImpl(OwnerRepository ownerRepo, PasswordUtils passwordUtils) {
        this.ownerRepo = ownerRepo;
        this.passwordUtils = passwordUtils;
    }

    @Override
    @Transactional
    public String verifyOwnerDetails(OwnerDetails ownerDetails) {
        log.info("{} is trying to login into Owner Dashboard",ownerDetails.getOwnerName());
        OwnerDetails ownerDetailsFromDatabase = ownerRepo.findByOwnerName(ownerDetails.getOwnerName());
        if (ownerDetailsFromDatabase == null){
            throw new OwnerException("Owner name is invalid");
        }

        String decryptedPassword = passwordUtils.decrypt(ownerDetailsFromDatabase.getPassword());

        if (!decryptedPassword.equals(ownerDetails.getPassword())) {
            throw new OwnerException("Password is Invalid");
        }
        log.info("Owner Authenticated Successfully");
        return "Owner Authenticated Successfully";
    }


    @Transactional
    public void addOwnerDetails(OwnerDetails ownerDetails) {
        if (ownerDetails == null){
            throw new OwnerException("Owner details are invalid");
        }
        boolean ownerExists = ownerRepo.existsByOwnerName(ownerDetails.getOwnerName());
        if (ownerExists) {
            return;
        }
        String encryptedPassword = passwordUtils.encrypt(ownerDetails.getPassword());
        ownerDetails.setPassword(encryptedPassword);

        ownerRepo.save(ownerDetails);
        log.info("Successfully saved the Owner details");
    }
}
