package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import sharespace.exception.RoomException;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.password.PasswordUtils;
import sharespace.payload.RoommateDTO;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepo;

    private final RoommateRepository roommateRepo;

    private final ModelMapper mapper;

    private final PasswordUtils passwordUtils;

    private static final int MAXIMUM_REFERRALS = 2;


    public RoomServiceImpl(RoomRepository roomRepo, RoommateRepository roommateRepo, ModelMapper mapper, PasswordUtils passwordUtils) {
        this.roomRepo = roomRepo;
        this.roommateRepo = roommateRepo;
        this.mapper = mapper;
        this.passwordUtils = passwordUtils;
    }

    @Override
    public List<Room> getAllRoomDetails() {
        List<Room> roomList = roomRepo.findAll();

        if (roomList.isEmpty()) {
            throw new RoomException("No Rooms are Added in the System");
        }

        return roomList;
    }

    @Override
    public Room getRoomById(int roomId) {
        return roomRepo.findById(roomId).orElseThrow(() ->
                new RoomException("Mentioned Room ID is not available"));
    }

    @Override
    public List<Room> checkAvailability(AvailabilityCheck available) {
        List<Room> roomList = roomRepo.findAll();
        if (roomList.isEmpty())
            throw new RoomException("No Rooms are Added in the System");
        List<Room> matchingRooms = new ArrayList<>();
        int capacity;
        for (Room room : roomList) {
            capacity = room.getCapacity() - room.getCurrentCapacity();
            if (available.getRoomType().equalsIgnoreCase(room.getRoomType()) &&
                    available.getCapacity() <= capacity &&
                    available.isWithAC() == room.getIsAcAvailable()) {
                matchingRooms.add(room);
            }
        }
        if (matchingRooms.isEmpty())
            throw new RoomException("Rooms are not available with your Condition");

        return matchingRooms;
    }

    @Override
    @Transactional
    public RoommateDTO bookRoom(int roomId, Roommate roommate) {

        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RoomException("Mentioned Room ID is not available"));

        if (roommate.getCheckOutDate() != null) {
            if (roommate.getCheckOutDate().isBefore(LocalDate.now())) {
                throw new RoomException("Check out date can't be entered in past");
            }
        }

        if (Objects.equals(room.getCapacity(), room.getCurrentCapacity()))
            throw new RoomException("Room was Full");


        roommate.setRoommateUniqueId(generateRoommateUniqueNumber(roommate.getUsername()));
        checkUsername(roommate);

        String encryptedPassword = passwordUtils.encrypt(roommate.getPassword());
        roommate.setPassword(encryptedPassword);

        if (roommate.getReferralId() != null && roommate.getReferralId().length() > 5)
            referralProcess(roommate);

        if (roommate.getWithFood()) {
            roommate.setRentAmount(room.getPrice());
        } else {
            roommate.setRentAmount(room.getPrice() - 1000);
        }
        roommate.setCheckInDate(LocalDate.now());
        roommate.setReferralId(generateReferId(roommate.getUsername()));
        roommate.setReferralCount(0);
        roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
        roommate.setRoomNumber(room.getRoomNumber());
        room.getRoommateList().add(roommate);
        room.setCurrentCapacity(room.getCurrentCapacity() + 1);
        roomRepo.save(room);

        return mapper.map(roommate, RoommateDTO.class);

    }

    @Transactional
    public void referralProcess(Roommate roommate) {

        Roommate referredRoommate = roommateRepo.findByReferralId(roommate.getReferralId());
        if (referredRoommate == null)
            throw new RoommateException("No Roommate matches with the entered Referral ID");
        if (referredRoommate.getReferralCount() > MAXIMUM_REFERRALS)
            throw new RoommateException("Already " + referredRoommate.getUsername() + " have reached max referrals");

        referredRoommate.setReferralCount(referredRoommate.getReferralCount() + 1);

        ReferralDetails referralDetails = new ReferralDetails();
        referralDetails.setUsername(roommate.getUsername());
        referralDetails.setReferralDate(LocalDate.now());
        referralDetails.setRoommateUniqueId(roommate.getRoommateUniqueId());

        referredRoommate.getReferralDetailsList().add(referralDetails);
        roommateRepo.save(referredRoommate);

    }

    public String generateRoommateUniqueNumber(String username) {
        return username.substring(0, 4) + UUID.randomUUID().toString().substring(0, 4);
    }

    public String generateReferId(String username) {
        return username + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public void checkUsername(Roommate roommate) {

        boolean usernameExists = roommateRepo.existsByUsernameIgnoreCase(roommate.getUsername());
        if (usernameExists) {
            throw new RoomException("Username Already Exists!!!");
        }
        boolean emailExists = roommateRepo.existsByEmailIgnoreCase(roommate.getEmail());
        if (emailExists) {
            throw new RoomException("Email ID Already Exists!!!");
        }
    }


    @Override
    @Transactional
    public String addRooms(Room room) {
        if (room == null)
            throw new RoomException("Invalid Room Details");
        if (checkRoomNumberExists(room.getRoomNumber()))
            throw new RoomException("Already this Room number : "+room.getRoomNumber()+" was taken");
        if (room.getCapacity()<=0)
            throw new RoomException("Total Capacity must be greater than 0. Provided: " + room.getCapacity());
        if (room.getCurrentCapacity()>room.getCapacity())
            throw new RoomException("Current capacity cannot be more than total capacity");
        if (room.getPrice()<1000)
            throw new RoomException("Room rent should be more than 1000");

        roomRepo.save(room);
        return "Room have been added Successfully";
    }

    @Override
    @Transactional
    public Room editRoom(int roomId, Room room) {
        if (room == null)
            throw new RoomException("Invalid Room Details");
        Room roomFromDatabase = roomRepo.findById(roomId).orElseThrow(() -> new RoomException("No Room found under this " + roomId + " Id"));

        if (room.getRoomNumber() != null) {
            if (checkRoomNumberExists(room.getRoomNumber()))
                throw new RoomException("Already this room number exists");
            roomFromDatabase.setRoomNumber(room.getRoomNumber());
        }
        if (room.getRoomType() != null) {
            roomFromDatabase.setRoomType(room.getRoomType());
        }
        if (room.getPrice() != null) {
            roomFromDatabase.setPrice(room.getPrice());
        }
        if (room.getFloorNumber() != null) {
            roomFromDatabase.setFloorNumber(room.getFloorNumber());
        }
        if (room.getCapacity() != null) {
            roomFromDatabase.setCapacity(room.getCapacity());
        }
        if (room.getCurrentCapacity() != null) {
            if (room.getCurrentCapacity() > roomFromDatabase.getCapacity()) {
                throw new RoomException("Current capacity cannot exceed total capacity");
            }
            roomFromDatabase.setCurrentCapacity(room.getCurrentCapacity());
        }
        if (room.getIsAcAvailable() != null) {
            roomFromDatabase.setIsAcAvailable(room.getIsAcAvailable());
        }

        return roomRepo.save(roomFromDatabase);
    }

    @Override
    @Transactional
    public String deleteRoom(int roomId) {
        Room room=roomRepo.findById(roomId).orElseThrow(() ->
                new RoomException("Mentioned Room Id is not available"));
        if (!room.getRoommateList().isEmpty())
            throw new RoomException("This room is not empty to delete");

        roomRepo.delete(room);
        return "Room deleted Successfully";
    }

    public Boolean checkRoomNumberExists(String roomNumber) {
        return roomRepo.existsByRoomNumber(roomNumber);
    }

}
