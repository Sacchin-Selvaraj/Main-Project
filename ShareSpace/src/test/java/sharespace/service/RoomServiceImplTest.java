package sharespace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import sharespace.exception.RoomException;
import sharespace.exception.RoommateException;
import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.password.PasswordUtils;
import sharespace.payload.RoommateDTO;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceImplTest {

    @Mock
    private RoommateRepository roommateRepo;

    @Mock
    private RoomRepository roomRepo;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room;
    private Roommate roommate;
    private RoommateDTO roommateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        room = new Room();
        room.setRoomId(1);
        room.setRoomNumber("F1");
        room.setRoomType("Single");
        room.setCapacity(2);
        room.setCurrentCapacity(1);
        room.setPrice(7000.0);
        room.setAcAvailable(true);

        roommate = new Roommate();
        roommate.setUsername("TestUser");
        roommate.setEmail("testuser@gmail.com");
        roommate.setPassword("encryptedPassword");
        roommate.setWithFood(true);
        roommate.setReferralId(null);

        roommateDTO = new RoommateDTO();
        roommateDTO.setUsername("TestUser");
        roommateDTO.setEmail("testuser@gmail.com");
        roommateDTO.setWithFood(true);

    }


    @Test
    void getAllRoomDetails() {

        when(roomRepo.findAll()).thenReturn(Collections.singletonList(room));

        List<Room> roomList=roomService.getAllRoomDetails();

        assertEquals(1,roomList.size());
        verify(roomRepo,times(1)).findAll();

    }
    @Test
    void getAllRoomDetails_NoRooms() {

        when(roomRepo.findAll()).thenReturn(List.of());

        RoomException exception=assertThrows(RoomException.class,() -> roomService.getAllRoomDetails());

        assertEquals("No Rooms are Added in the System",exception.getMessage());
        verify(roomRepo,times(1)).findAll();

    }

    @Test
    void getRoomById() {
        when(roomRepo.findById(1)).thenReturn(Optional.of(room));

        Room foundRoom = roomService.getRoomById(1);

        assertNotNull(foundRoom);
        assertEquals("F1", foundRoom.getRoomNumber());
        verify(roomRepo, times(1)).findById(1);
    }

    @Test
    void getRoomById_NotFound() {
        when(roomRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(RoomException.class, () -> roomService.getRoomById(1));
        verify(roomRepo, times(1)).findById(1);
    }
    @Test
    void checkAvailability() {
        AvailabilityCheck availability = new AvailabilityCheck();
        availability.setRoomType("Single");
        availability.setCapacity(1);
        availability.setWithAC(true);

        when(roomRepo.findAll()).thenReturn(Collections.singletonList(room));

        List<Room> availableRooms = roomService.checkAvailability(availability);

        assertNotNull(availableRooms);
        assertEquals(1, availableRooms.size());
        verify(roomRepo, times(1)).findAll();
    }

    @Test
    void checkAvailability_NoRooms() {
        AvailabilityCheck availability = new AvailabilityCheck();
        availability.setRoomType("Single");
        availability.setCapacity(1);
        availability.setWithAC(true);

        when(roomRepo.findAll()).thenReturn(Collections.emptyList());

        assertThrows(RoomException.class, () -> roomService.checkAvailability(availability));
        verify(roomRepo, times(1)).findAll();
    }

    @Test
    void bookRoom() {

        when(roomRepo.findById(1)).thenReturn(Optional.of(room));
        when(passwordUtils.encrypt(anyString())).thenReturn("encryptedPassword");
        when(roomRepo.save(room)).thenReturn(room);
        when(modelMapper.map(roommate,RoommateDTO.class)).thenReturn(roommateDTO);

        RoommateDTO roommateDTO1=roomService.bookRoom(1,roommate);

        assertNotNull(roommateDTO1);
        assertEquals("TestUser",roommateDTO1.getUsername());
        verify(roomRepo,times(1)).findById(1);
        verify(roomRepo,times(1)).save(room);
        verify(passwordUtils,times(1)).encrypt(roommate.getPassword());

    }

    @Test
    void bookRoom_RoomFull() {
        room.setCurrentCapacity(2);

        when(roomRepo.findById(1)).thenReturn(Optional.of(room));

        assertThrows(RoomException.class, () -> roomService.bookRoom(1, roommate));
        verify(roomRepo, times(1)).findById(1);
    }
    @Test
    void bookRoom_UsernameExists() {

        when(roomRepo.findById(1)).thenReturn(Optional.of(room));
        when(roommateRepo.existsByUsernameIgnoreCase("TestUser")).thenReturn(true);

        assertThrows(RoomException.class, () -> roomService.bookRoom(1, roommate));
        verify(roomRepo, times(1)).findById(1);
        verify(roommateRepo, times(1)).existsByUsernameIgnoreCase("TestUser");

    }

    @Test
    void referralProcess(){
        String referralId="testuser-wrg53";
        roommate.setReferralId(referralId);

        Roommate referredRoommate = new Roommate();
        referredRoommate.setUsername("user2");
        referredRoommate.setEmail("testuser2@gmail.com");
        referredRoommate.setPassword("encryptedPassword");
        referredRoommate.setWithFood(true);
        referredRoommate.setReferralId(null);
        referredRoommate.setReferralDetailsList(new ArrayList<>());

        when(roommateRepo.findByReferralId(referralId)).thenReturn(referredRoommate);

        roomService.referralProcess(roommate);

        verify(roommateRepo,times(1)).findByReferralId(referralId);
        verify(roommateRepo,times(1)).save(referredRoommate);
    }

    @Test
    void referralProcess_NoMatchingRoommate(){
        roommate.setReferralId("referral123");

        when(roommateRepo.findByReferralId("referral123")).thenReturn(null);

        RoommateException exception = assertThrows(RoommateException.class,
                () -> roomService.referralProcess(roommate));
        assertEquals("No Roommate matches with the entered Referral ID", exception.getMessage());
        verify(roommateRepo,times(1)).findByReferralId("referral123");
    }

    @Test
    void referralProcess_MaxReferralExceed() {
        roommate.setReferralId("referral123");

        Roommate referredRoommate = new Roommate();
        referredRoommate.setUsername("user2");
        referredRoommate.setEmail("testuser2@gmail.com");
        referredRoommate.setPassword("encryptedPassword");
        referredRoommate.setWithFood(true);
        referredRoommate.setReferralCount(3);
        referredRoommate.setReferralDetailsList(new ArrayList<>());

        when(roommateRepo.findByReferralId("referral123")).thenReturn(referredRoommate);

        RoommateException exception = assertThrows(RoommateException.class,
                () -> roomService.referralProcess(roommate));
        assertEquals("Already "+referredRoommate.getUsername()+" have reached max referrals", exception.getMessage());
        verify(roommateRepo,times(1)).findByReferralId("referral123");

    }

}