package sharespace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.password.PasswordUtils;
import sharespace.payload.RoommateDTO;
import sharespace.payload.VacateResponseDTO;
import sharespace.repository.PaymentRepository;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;
import sharespace.repository.VacateRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoommateServiceImplTest {

    @Mock
    private RoommateRepository roommateRepo;

    @Mock
    private RoomRepository roomRepo;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private VacateRepository vacateRepo;

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoommateServiceImpl roommateService;

    private Roommate roommate1;
    private Roommate roommate2;
    private RoommateDTO roommateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roommate1=new Roommate();
        roommate1.setUsername("TestUser1");
        roommate1.setPassword("user1");
        roommate1.setEmail("test123@gmail.com");
        roommate1.setRentAmount(7000);
        roommate2=new Roommate();
        roommate2.setUsername("TestUser2");
        roommate2.setPassword("user2");
        roommate2.setEmail("test1234@gmail.com");
        roommate2.setRentAmount(6000);

        roommateDTO=new RoommateDTO();
        roommateDTO.setUsername("TestUser3");
        roommateDTO.setPassword("user3");
        roommateDTO.setEmail("test12345@gmail.com");
        roommateDTO.setRentAmount(5000);

    }


    @Test
    void getAllRoommates() {

        List<Roommate> roommates= Arrays.asList(roommate1,roommate2);
        when(roommateRepo.findAll()).thenReturn(roommates);
        when(modelMapper.map(roommate1,RoommateDTO.class)).thenReturn(roommateDTO);

        List<RoommateDTO> roommateList=roommateService.getAllRoommates();

        assertEquals(2,roommateList.size());
        assertEquals("TestUser3", roommateList.get(0).getUsername());
        verify(roommateRepo,times(1)).findAll();

    }

    @Test
    void getAllRoommates_NoRoommates() {

        when(roommateRepo.findAll()).thenReturn(new ArrayList<>());

        RoommateException exception=assertThrows(RoommateException.class,() -> roommateService.getAllRoommates());

        assertEquals("No Roommate available",exception.getMessage());
        verify(roommateRepo,times(1)).findAll();

    }

    @Test
    void updateEmail() {
        Roommate roommate1=new Roommate();
        roommate1.setUsername("TestUser1");
        roommate1.setPassword("user1");
        roommate1.setEmail("test123@gmail.com");
        roommate1.setRentAmount(7000);

        when(roommateRepo.findById(1)).thenReturn(Optional.of(roommate1));
        when(roommateRepo.save(roommate1)).thenReturn(roommate1);

        String response=roommateService.updateEmail(1,roommate1.getEmail());

        assertEquals("Email updated successfully for roommate",response);
        verify(roommateRepo,times(1)).findById(1);
        verify(roommateRepo,times(1)).save(roommate1);

    }
    @Test
    void updateEmail_NoRoommate() {

        int id = 1;

        when(roommateRepo.findById(id)).thenReturn(Optional.empty());

        RoommateException roommateException= assertThrows(RoommateException.class,() -> roommateService.updateEmail(1,"test123@gmail"));

        assertEquals("Roommate not found with id 1",roommateException.getMessage());
        verify(roommateRepo, times(1)).findById(id);
        verify(roommateRepo,never()).save(any());
    }

    @Test
    void updateRoommate() {
        int id = 1;
        Roommate roommate = new Roommate();
        roommate.setUsername("TestUser");
        roommate.setPassword("test123");

        Roommate existingRoommate = new Roommate();
        existingRoommate.setRoommateId(id);
        existingRoommate.setUsername(roommate.getUsername());
        existingRoommate.setPassword(roommate.getPassword());

        when(roommateRepo.findById(id)).thenReturn(Optional.of(existingRoommate));
        when(roommateRepo.save(existingRoommate)).thenReturn(existingRoommate);

        String result = roommateService.updateRoommate(id, roommate);

        assertEquals("Roommate details updated successfully", result);
        verify(roommateRepo, times(1)).findById(id);
        verify(roommateRepo, times(1)).save(existingRoommate);

    }

    @Test
    void updateRoommate_RoommateNotFound() {
        int id = 1;
        Roommate roommate = new Roommate();

        when(roommateRepo.findById(id)).thenReturn(Optional.empty());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.updateRoommate(id, roommate);
        });
        assertEquals("Roommate not found with id " + id, exception.getMessage());
        verify(roommateRepo, times(1)).findById(id);
        verify(roommateRepo, never()).save(any());
    }

    @Test
    void getRoommate() {
        LoginDetails loginDetails=new LoginDetails("user1","user@123");
        Roommate roommate1=new Roommate();
        roommate1.setUsername("user1");
        roommate1.setPassword("wertyujnbvfde34567");
        roommate1.setEmail("test123@gmail.com");
        roommate1.setRentAmount(7000);

        when(roommateRepo.findByUsername(loginDetails.getUsername())).thenReturn(roommate1);
        when(passwordUtils.decrypt(roommate1.getPassword())).thenReturn("user@123");

        Roommate roommate=roommateService.getRoommate(loginDetails);

        assertEquals("user1",roommate.getUsername());
        verify(roommateRepo,times(1)).findByUsername(loginDetails.getUsername());
        verify(passwordUtils,times(1)).decrypt(roommate1.getPassword());
        verify(passwordUtils,never()).encrypt(any());

    }

    @Test
    void getRoommate_InvalidUsername() {
        LoginDetails loginDetails = new LoginDetails();
        loginDetails.setUsername("invalidUser");
        loginDetails.setPassword("password123");

        when(roommateRepo.findByUsername("invalidUser")).thenReturn(null);

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.getRoommate(loginDetails);
        });
        assertEquals("Username is invalid", exception.getMessage());
        verify(roommateRepo, times(1)).findByUsername("invalidUser");
        verify(passwordUtils, never()).decrypt(any());

    }

    @Test
    void deleteRoommate() {
        String username="Testuser";
        Roommate roommate1=new Roommate();
        roommate1.setUsername("Testuser");
        roommate1.setPassword("wertyujnbvfde34567");
        roommate1.setEmail("test123@gmail.com");
        roommate1.setRentAmount(7000);
        roommate1.setRoomNumber("F1");

        Room room=new Room();
        room.setRoomNumber("F1");
        room.setCurrentCapacity(3);
        room.setPrice(7000.00);

        when(roommateRepo.findByUsername(username)).thenReturn(roommate1);
        when(roomRepo.findByRoomNumber(roommate1.getRoomNumber())).thenReturn(room);

        roommateService.deleteRoommate(username);

        assertEquals(2, room.getCurrentCapacity());
        verify(roommateRepo, times(1)).findByUsername(username);
        verify(roomRepo, times(1)).findByRoomNumber("F1");
        verify(roomRepo, times(1)).save(room);
        verify(roommateRepo, times(1)).delete(roommate1);

    }

    @Test
    void deleteRoommate_RoommateNotFound() {

        String username = "invalidUser";

        when(roommateRepo.findByUsername(username)).thenReturn(null);

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.deleteRoommate(username);
        });
        assertEquals("No Roommate present under this Username", exception.getMessage());
        verify(roommateRepo, times(1)).findByUsername(username);
        verify(roomRepo, never()).findByRoomNumber(any());
        verify(roomRepo, never()).save(any());
        verify(roommateRepo, never()).delete(any());
    }

    @Test
    void updateDetails() {

        int roommateId = 1;
        UpdateDetails updateDetails = new UpdateDetails();
        updateDetails.setUsername("newUsername");
        updateDetails.setEmail("newemail@example.com");
        updateDetails.setPassword("newPassword");
        updateDetails.setWithFood(true);
        updateDetails.setCheckOutDate(LocalDate.now());

        Roommate roommate = new Roommate();
        roommate.setRoommateId(roommateId);
        roommate.setUsername("oldUsername");
        roommate.setEmail("oldemail@example.com");
        roommate.setPassword("oldPassword");
        roommate.setWithFood(false);
        roommate.setRentAmount(1000);
        roommate.setLastModifiedDate(LocalDate.now().minusDays(29));

        when(roommateRepo.findById(roommateId)).thenReturn(Optional.of(roommate));
        when(passwordUtils.encrypt("newPassword")).thenReturn("encryptedPassword");
        when(roommateRepo.save(roommate)).thenReturn(roommate);
        when(modelMapper.map(roommate,RoommateDTO.class)).thenReturn(roommateDTO);


        RoommateDTO result = roommateService.updateDetails(roommateId, updateDetails);

        assertEquals("TestUser3", result.getUsername());
        assertEquals("test12345@gmail.com", result.getEmail());
        assertEquals("user3", result.getPassword());
        assertEquals(5000, result.getRentAmount());
        verify(roommateRepo, times(1)).findById(roommateId);
        verify(roommateRepo, times(1)).save(roommate);
    }

    @Test
    void updateDetails_RoommateNotFound() {

        int roommateId = 1;
        UpdateDetails updateDetails = new UpdateDetails();

        when(roommateRepo.findById(roommateId)).thenReturn(Optional.empty());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.updateDetails(roommateId, updateDetails);
        });
        assertEquals("No Roommate found under this Id", exception.getMessage());
        verify(roommateRepo, times(1)).findById(roommateId);
        verify(roommateRepo, never()).save(any());
    }

    @Test
    void sendVacateRequest() {
        int roommateId = 1;
        VacateRequest vacateRequest = new VacateRequest();
        vacateRequest.setCheckOutDate(LocalDate.now().plusDays(5));

        Roommate roommate = new Roommate();
        roommate.setRoommateId(roommateId);

        when(roommateRepo.findById(roommateId)).thenReturn(Optional.of(roommate));

        String result = roommateService.sendVacateRequest(roommateId, vacateRequest);

        assertEquals("Vacate Request Sent Successfully", result);
        verify(roommateRepo, times(1)).findById(roommateId);
        verify(vacateRepo, times(1)).save(vacateRequest);
        verify(roommateRepo, times(1)).save(roommate);

    }

    @Test
    void sendVacateRequest_RoommateNotFound() {

        int roommateId = 1;
        VacateRequest vacateRequest = new VacateRequest();

        when(roommateRepo.findById(roommateId)).thenReturn(Optional.empty());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.sendVacateRequest(roommateId, vacateRequest);
        });
        assertEquals("No Roommate found under this Id", exception.getMessage());
        verify(roommateRepo, times(1)).findById(roommateId);
        verify(vacateRepo, never()).save(any());
        verify(roommateRepo, never()).save(any());
    }

    @Test
    void SendVacateRequest_CheckOutDateInPast() {

        int roommateId = 1;
        VacateRequest vacateRequest = new VacateRequest();
        vacateRequest.setCheckOutDate(LocalDate.now().minusDays(5));

        Roommate roommate = new Roommate();
        roommate.setRoommateId(roommateId);

        when(roommateRepo.findById(roommateId)).thenReturn(Optional.of(roommate));

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.sendVacateRequest(roommateId, vacateRequest);
        });
        assertEquals("CheckOut Date can't be in Past :"+LocalDate.now().minusDays(5), exception.getMessage());
        verify(roommateRepo, times(1)).findById(roommateId);
        verify(vacateRepo, never()).save(any());
        verify(roommateRepo, never()).save(any());
    }
    @Test
    void getPendingVacateRequests() {
        VacateRequest vacateRequest1 = new VacateRequest();
        vacateRequest1.setVacateRequestId(1);

        VacateRequest vacateRequest2 = new VacateRequest();
        vacateRequest2.setVacateRequestId(2);

        List<VacateRequest> vacateRequests = Arrays.asList(vacateRequest1, vacateRequest2);

        VacateResponseDTO vacateResponseDTO1 = new VacateResponseDTO();
        vacateResponseDTO1.setRoommateName("user1");

        VacateResponseDTO vacateResponseDTO2 = new VacateResponseDTO();
        vacateResponseDTO2.setRoommateName("user2");

        TypeMap<VacateRequest, VacateResponseDTO> typeMap=mock(TypeMap.class);
        when(vacateRepo.findByIsReadFalse()).thenReturn(vacateRequests);
        when(modelMapper.map(vacateRequest1, VacateResponseDTO.class)).thenReturn(vacateResponseDTO1);
        when(modelMapper.map(vacateRequest2, VacateResponseDTO.class)).thenReturn(vacateResponseDTO2);
        when(modelMapper.typeMap(VacateRequest.class, VacateResponseDTO.class)).thenReturn(typeMap);

        List<VacateResponseDTO> result = roommateService.getPendingVacateRequests();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getRoommateName());
        assertEquals("user2", result.get(1).getRoommateName());
        verify(vacateRepo, times(1)).findByIsReadFalse();

    }

    @Test
    void GetPendingVacateRequests_NoRequests() {
        when(vacateRepo.findByIsReadFalse()).thenReturn(List.of());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.getPendingVacateRequests();
        });
        assertEquals("No Vacate Request so Far", exception.getMessage());
        verify(vacateRepo, times(1)).findByIsReadFalse();
    }

    @Test
    void markAsRead() {
        VacateRequest vacateRequest=new VacateRequest();
        vacateRequest.setVacateRequestId(1);
        vacateRequest.setIsRead(false);

        when(vacateRepo.findById(1)).thenReturn(Optional.of(vacateRequest));

        roommateService.markAsRead(1);

        verify(vacateRepo,times(1)).delete(vacateRequest);
        verify(vacateRepo,times(1)).findById(1);

    }

    @Test
    void markAsRead_InvalidVacateId() {
        int requestId = 1;

        when(vacateRepo.findById(requestId)).thenReturn(Optional.empty());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            roommateService.markAsRead(requestId);
        });
        assertEquals("Vacate request not found", exception.getMessage());
        verify(vacateRepo, times(1)).findById(requestId);
        verify(vacateRepo, never()).delete(any());
    }

}