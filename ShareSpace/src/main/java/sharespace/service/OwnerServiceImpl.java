package sharespace.service;

import sharespace.exception.OwnerException;
import sharespace.model.OwnerDetails;
import sharespace.password.PasswordUtils;
import sharespace.repository.OwnerRepository;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl implements OwnerService{

    private final OwnerRepository ownerRepo;

    private final PasswordUtils passwordUtils;

    public OwnerServiceImpl(OwnerRepository ownerRepo, PasswordUtils passwordUtils) {
        this.ownerRepo = ownerRepo;
        this.passwordUtils = passwordUtils;
    }

    @Override
    public String verifyOwnerDetails(OwnerDetails ownerDetails) {

        System.out.println(ownerDetails.getOwnerName());
        OwnerDetails ownerDetailsFromDatabase=ownerRepo.findByOwnerName(ownerDetails.getOwnerName());
        if (ownerDetailsFromDatabase==null)
            throw new OwnerException("Owner username is invalid");

        String decryptedPassword=passwordUtils.decrypt(ownerDetailsFromDatabase.getPassword());

        if (!decryptedPassword.equals(ownerDetails.getPassword())){
            throw new OwnerException("Password is Invalid");
        }

        return "Owner Authenticated Successfully";
    }


    public void addOwnerDetails(OwnerDetails ownerDetails){
        if (ownerDetails==null)
            throw new OwnerException("Owner details are invalid");
        String encryptedPassword=passwordUtils.encrypt(ownerDetails.getPassword());
        ownerDetails.setPassword(encryptedPassword);

        ownerRepo.save(ownerDetails);
    }
}
