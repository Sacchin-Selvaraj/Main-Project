package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sharespace.constants.RoomConstants;
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

    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    public RoomServiceImpl(RoomRepository roomRepo, RoommateRepository roommateRepo, ModelMapper mapper, PasswordUtils passwordUtils) {
        this.roomRepo = roomRepo;
        this.roommateRepo = roommateRepo;
        this.mapper = mapper;
        this.passwordUtils = passwordUtils;
    }

    @Override
    public List<Room> getAllRoomDetails() {
        log.info("Fetching all rooms from the database");
        List<Room> roomList = roomRepo.findAll();

        if (roomList.isEmpty()) {
            log.warn("No rooms found in the system");
            throw new RoomException("No Rooms are Added in the System");
        }
        log.info("Successfully fetched {} rooms", roomList.size());
        return roomList;
    }

    @Override
    public Room getRoomById(int roomId) {
        log.info("Fetching room by Id: {}", roomId);
        return roomRepo.findById(roomId).orElseThrow(() ->{
            log.error("Room not found with Id: {}", roomId);
            return new RoomException("Mentioned Room Id is not available");
        });
    }

    @Override
    public List<Room> checkAvailability(AvailabilityCheck available) {
        log.info("Checking room availability for: {}", available.getRoomType());
        List<Room> roomList = roomRepo.findAll();
        if (roomList.isEmpty()) {
            log.warn("No rooms found in the Server");
            throw new RoomException("No Rooms are Added in the System");
        }
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
        if (matchingRooms.isEmpty()) {
            log.warn("No rooms matching the condition");
            throw new RoomException("Rooms are not available with your Condition");
        }
        log.info("Found {} matching rooms", matchingRooms.size());
        return matchingRooms;
    }

    @Override
    @Transactional
    public RoommateDTO bookRoom(int roomId, Roommate roommate) {
        log.info("Attempting to book room with Id: {} for roommate: {}", roomId, roommate.getUsername());
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RoomException("Mentioned Room ID is not available"));

        if (roommate.getCheckOutDate() != null&&roommate.getCheckOutDate().isBefore(LocalDate.now())) {
            log.error("Invalid checkout date: {}", roommate.getCheckOutDate());
            throw new RoomException("Checkout date can't be entered as past date");
        }

        if (Objects.equals(room.getCapacity(), room.getCurrentCapacity())) {
            log.error("Room is full with ID: {}", roomId);
            throw new RoomException("Room was Full");
        }

        roommate.setRoommateUniqueId(generateRoommateUniqueNumber(roommate.getUsername()));
        log.info("checkUsername and Email already exists or not");
        checkUsernameEmail(roommate);

        String encryptedPassword = passwordUtils.encrypt(roommate.getPassword());
        roommate.setPassword(encryptedPassword);

        log.info("Checking for the valid referral");
        if (roommate.getReferralId() != null && roommate.getReferralId().length() > RoomConstants.REFERRAL_LENGTH)
            referralProcess(roommate);

        if (roommate.getWithFood()) {
            roommate.setRentAmount(room.getPrice());
        } else {
            roommate.setRentAmount(room.getPrice() - RoomConstants.WITHOUT_FOOD);
        }
        roommate.setCheckInDate(LocalDate.now());
        roommate.setReferralId(generateReferId(roommate.getUsername()));
        roommate.setReferralCount(RoomConstants.NULL_VALUE);
        roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
        roommate.setRoomNumber(room.getRoomNumber());
        room.getRoommateList().add(roommate);
        room.setCurrentCapacity(room.getCurrentCapacity() + 1);
        roomRepo.save(room);
        log.info("Room booked successfully for roommate: {}", roommate.getUsername());
        return mapper.map(roommate, RoommateDTO.class);

    }

    @Transactional
    public void referralProcess(Roommate roommate) {
        log.info("Processing referral for roommate: {}", roommate.getUsername());
        Roommate referredRoommate = roommateRepo.findByReferralId(roommate.getReferralId());
        if (referredRoommate == null) {
            log.error("No roommate found with referral ID: {}", roommate.getReferralId());
            throw new RoommateException("No Roommate matches with the entered Referral ID");
        }
        if (referredRoommate.getReferralCount() > RoomConstants.MAXIMUM_REFERRALS) {
            log.error("Referral count exceeded for roommate: {}", referredRoommate.getUsername());
            throw new RoommateException("Already " + referredRoommate.getUsername() + " have reached max referrals");
        }
        referredRoommate.setReferralCount(referredRoommate.getReferralCount() + 1);

        ReferralDetails referralDetails = new ReferralDetails();
        referralDetails.setUsername(roommate.getUsername());
        referralDetails.setReferralDate(LocalDate.now());
        referralDetails.setRoommateUniqueId(roommate.getRoommateUniqueId());

        referredRoommate.getReferralDetailsList().add(referralDetails);

        log.info("Referral processed successfully for roommate: {}", roommate.getUsername());
        roommateRepo.save(referredRoommate);

    }

    public String generateRoommateUniqueNumber(String username) {
        log.info("Generating unique roommate number for username: {}", username);
        return username.substring(0, 4) + UUID.randomUUID().toString().substring(0, 4);
    }

    public String generateReferId(String username) {
        log.info("Generating referral Id for username: {}", username);
        return username + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public void checkUsernameEmail(Roommate roommate) {
        log.info("Checking if username or email already exists for roommate: {}", roommate.getUsername());
        boolean usernameExists = roommateRepo.existsByUsernameIgnoreCase(roommate.getUsername());
        if (usernameExists) {
            log.error("Username already exists: {}", roommate.getUsername());
            throw new RoomException("Username Already Exists!!!");
        }
        boolean emailExists = roommateRepo.existsByEmailIgnoreCase(roommate.getEmail());
        if (emailExists) {
            log.error("Email already exists: {}", roommate.getEmail());
            throw new RoomException("Email ID Already Exists!!!");
        }
    }

    @Override
    @Transactional
    public String addRooms(Room room) {
        log.info("Attempting to add a room");
        if (room == null) {
            log.error("Invalid room details provided");
            throw new RoomException("Invalid Room Details");
        }
        if (checkRoomNumberExists(room.getRoomNumber())) {
            log.error("Room number already exists: {}", room.getRoomNumber());
            throw new RoomException("Already this Room number : " + room.getRoomNumber() + " was taken");
        }
        if (room.getCapacity() <= RoomConstants.NULL_VALUE) {
            log.error("Invalid capacity provided: {}", room.getCapacity());
            throw new RoomException("Total Capacity must be greater than 0. Provided : " + room.getCapacity());
        }
        if (room.getCurrentCapacity() > room.getCapacity()) {
            log.error("Current capacity exceeds total capacity: {}", room.getCurrentCapacity());
            throw new RoomException("Current capacity cannot be more than total capacity");
        }
        if (room.getPrice() < RoomConstants.WITHOUT_FOOD) {
            log.error("Invalid room price provided: {}", room.getPrice());
            throw new RoomException("Room rent should be more than 1000");
        }

        roomRepo.save(room);
        log.info("Room added successfully: {}", room.getRoomNumber());
        return "Room have been added Successfully";

    }

    @Override
    @Transactional
    public Room editRoom(int roomId, Room room) {
        log.info("Attempting to edit a room with Id : {}",roomId);
        if (room == null) {
            log.error("Invalid room Id provided");
            throw new RoomException("Invalid Room Details");
        }
        Room roomFromDatabase = roomRepo.findById(roomId).orElseThrow(() -> {
            log.error("Room not found with ID: {}", roomId);
            return new RoomException("No Room found under this " + roomId + " Id");
        });

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
            if (room.getCapacity()<RoomConstants.NULL_VALUE)
                throw new RoomException("Total capacity cannot be less than 0");
            roomFromDatabase.setCapacity(room.getCapacity());
        }
        if (room.getCurrentCapacity() != null) {
            if (room.getCurrentCapacity() > roomFromDatabase.getCapacity()) {
                log.error("Current capacity exceed total capacity: {}", room.getCurrentCapacity());
                throw new RoomException("Current capacity cannot exceed total capacity");
            }
            roomFromDatabase.setCurrentCapacity(room.getCurrentCapacity());
        }
        if (room.getIsAcAvailable() != null) {
            roomFromDatabase.setIsAcAvailable(room.getIsAcAvailable());
        }
        log.info("Room updated successfully with ID: {}", roomId);
        return roomRepo.save(roomFromDatabase);
    }

    @Override
    @Transactional
    public String deleteRoom(int roomId) {
        log.info("Attempting to delete room with ID: {}", roomId);
        Room room=roomRepo.findById(roomId).orElseThrow(() -> {
            log.error("Room not found with Id : {}", roomId);
            return new RoomException("Mentioned Room Id is not available");
        });
        if (!room.getRoommateList().isEmpty())
            throw new RoomException("This room is not empty to delete");

        roomRepo.delete(room);
        log.info("Room deleted successfully with Id: {}", roomId);
        return "Room deleted Successfully";
    }

    public Boolean checkRoomNumberExists(String roomNumber) {
        log.debug("Checking if room number exists: {}", roomNumber);
        return roomRepo.existsByRoomNumber(roomNumber);
    }

}
