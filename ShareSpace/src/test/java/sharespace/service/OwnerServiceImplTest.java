package sharespace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sharespace.exception.OwnerException;
import sharespace.model.OwnerDetails;
import sharespace.password.PasswordUtils;
import sharespace.repository.OwnerRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerServiceImplTest {

    @Mock
    private OwnerRepository ownerRepo;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testVerifyOwnerDetails_Success() {
        OwnerDetails ownerDetails = new OwnerDetails();
        ownerDetails.setOwnerName("owner1");
        ownerDetails.setPassword("password123");

        OwnerDetails ownerDetailsFromDatabase = new OwnerDetails();
        ownerDetailsFromDatabase.setOwnerName("owner1");
        ownerDetailsFromDatabase.setPassword("encryptedPassword");

        when(ownerRepo.findByOwnerName("owner1")).thenReturn(ownerDetailsFromDatabase);
        when(passwordUtils.decrypt("encryptedPassword")).thenReturn("password123");

        String result = ownerService.verifyOwnerDetails(ownerDetails);

        assertEquals("Owner Authenticated Successfully", result);
        verify(ownerRepo, times(1)).findByOwnerName("owner1");
        verify(passwordUtils, times(1)).decrypt("encryptedPassword");
    }

    @Test
    void testVerifyOwnerDetails_InvalidUsername() {
        OwnerDetails ownerDetails = new OwnerDetails();
        ownerDetails.setOwnerName("invalidOwner");
        ownerDetails.setPassword("password123");

        when(ownerRepo.findByOwnerName("invalidOwner")).thenReturn(null);

        OwnerException exception = assertThrows(OwnerException.class, () -> {
            ownerService.verifyOwnerDetails(ownerDetails);
        });
        assertEquals("Owner name is invalid", exception.getMessage());
        verify(ownerRepo, times(1)).findByOwnerName("invalidOwner");
        verify(passwordUtils, never()).decrypt(any());
    }

    @Test
    void testVerifyOwnerDetails_InvalidPassword() {
        OwnerDetails ownerDetails = new OwnerDetails();
        ownerDetails.setOwnerName("owner1");
        ownerDetails.setPassword("wrongPassword");

        OwnerDetails ownerDetailsFromDatabase = new OwnerDetails();
        ownerDetailsFromDatabase.setOwnerName("owner1");
        ownerDetailsFromDatabase.setPassword("encryptedPassword");

        when(ownerRepo.findByOwnerName("owner1")).thenReturn(ownerDetailsFromDatabase);
        when(passwordUtils.decrypt("encryptedPassword")).thenReturn("password123");

        OwnerException exception = assertThrows(OwnerException.class, () -> {
            ownerService.verifyOwnerDetails(ownerDetails);
        });
        assertEquals("Password is Invalid", exception.getMessage());
        verify(ownerRepo, times(1)).findByOwnerName("owner1");
        verify(passwordUtils, times(1)).decrypt("encryptedPassword");
    }

    @Test
    void testAddOwnerDetails_Success() {
        OwnerDetails ownerDetails = new OwnerDetails();
        ownerDetails.setOwnerName("owner2");
        ownerDetails.setPassword("password123");

        when(passwordUtils.encrypt("password123")).thenReturn("encryptedPassword");

        ownerService.addOwnerDetails(ownerDetails);

        verify(passwordUtils, times(1)).encrypt("password123");
        verify(ownerRepo, times(1)).save(ownerDetails);
    }

    @Test
    void testAddOwnerDetails_NullOwnerDetails() {
        OwnerDetails ownerDetails = null;

        OwnerException exception = assertThrows(OwnerException.class, () -> {
            ownerService.addOwnerDetails(ownerDetails);
        });
        assertEquals("Owner details are invalid", exception.getMessage());
        verify(passwordUtils, never()).encrypt(any());
        verify(ownerRepo, never()).save(any());
    }
}