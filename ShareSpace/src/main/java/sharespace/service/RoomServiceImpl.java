package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import sharespace.constants.RoomConstants;
import sharespace.exception.RoomException;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.password.PasswordUtils;
import sharespace.payload.OwnerRoomDTO;
import sharespace.payload.RoomDTO;
import sharespace.payload.RoommateDTO;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

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
    @Cacheable(value = "RoomDetailsCache",key = "'allrooms'")
    public List<OwnerRoomDTO> getAllRoomDetails() {
        List<Room> roomList = roomRepo.findAll();

        if (roomList.isEmpty()) {
            throw new RoomException("No Rooms are Added in the System");
        }
        log.info("Successfully fetched {} rooms", roomList.size());

        List<OwnerRoomDTO> ownerRoomDTOS=roomList.stream().map(room -> {
            OwnerRoomDTO ownerRoomDTO=mapper.map(room, OwnerRoomDTO.class);
            List<Roommate> roommateList = room.getRoommateList();
            if (!roommateList.isEmpty()) {
                List<RoommateDTO> roommateDTOS = roommateList.stream()
                        .map(roommate -> mapper.map(roommate, RoommateDTO.class))
                        .toList();
                ownerRoomDTO.setRoommateDTO(roommateDTOS);
            } else {
                ownerRoomDTO.setRoommateDTO(Collections.emptyList());
            }
            return ownerRoomDTO;
        }).toList();

        return ownerRoomDTOS;
    }

    @Override
    public RoomDTO getRoomById(int roomId) {
        Room room=roomRepo.findById(roomId).orElseThrow(() -> new RoomException("Mentioned Room Id is not available"));

        return mapper.map(room,RoomDTO.class);
    }

    @Override
    public List<RoomDTO> checkAvailability(AvailabilityCheck available) {
        List<Room> roomList = roomRepo.findAll();
        if (roomList.isEmpty()) {
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
            throw new RoomException("Rooms are not available with your Condition");
        }
        log.info("Found {} matching rooms", matchingRooms.size());
        return matchingRooms.stream().map(room -> mapper.map(room,RoomDTO.class)).toList();
    }

    @Override
    @Transactional
    public String bookRoom(int roomId, Roommate roommate) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RoomException("Mentioned Room ID is not available"));

        if (roommate.getCheckOutDate() != null && roommate.getCheckOutDate().isBefore(LocalDate.now())) {
            throw new RoomException("Checkout date can't be entered as past date");
        }

        if (Objects.equals(room.getCapacity(), room.getCurrentCapacity())) {
            throw new RoomException("Room was Full");
        }

        roommate.setRoommateUniqueId(generateRoommateUniqueNumber(roommate.getUsername()));
        checkUsernameEmail(roommate);

        String encryptedPassword = passwordUtils.encrypt(roommate.getPassword());
        roommate.setPassword(encryptedPassword);

        if (roommate.getReferralId() != null && roommate.getReferralId().length() > RoomConstants.REFERRAL_LENGTH)
            referralProcess(roommate);

        LocalDate fifthDayOfMonth = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 5);
        log.info("5th day of the month: {}", fifthDayOfMonth);
        if (roommate.getCheckInDate().isAfter(fifthDayOfMonth) && roommate.getCheckInDate().getMonth().equals(fifthDayOfMonth.getMonth())) {
            log.info("Calculating partial rent for check-in after the 5th of the month");
            roommate.setRentAmount(calculatePartialRentAmount(room.getRoomNumber(), roommate.getCheckInDate(),roommate.getWithFood()));

        } else {
            log.info("Setting full rent for check-in on or before the 5th of the month");
            roommate.setRentAmount(roommate.getWithFood() ? room.getPrice() : room.getPrice() - RoomConstants.WITHOUT_FOOD);
        }
        roommate.setCheckInDate(roommate.getCheckInDate());
        roommate.setLastModifiedDate(LocalDate.now());
        roommate.setReferralId(generateReferId(roommate.getUsername()));
        roommate.setReferralCount(RoomConstants.NULL_VALUE);
        roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
        roommate.setRoomNumber(room.getRoomNumber());
        room.getRoommateList().add(roommate);
        room.setCurrentCapacity(room.getCurrentCapacity() + 1);
        roommateRepo.save(roommate);
        roomRepo.save(room);
        log.info("Room booked successfully for roommate: {}", roommate.getUsername());
        return "Room booked successfully for roommate: "+roommate.getUsername();

    }

    private double calculatePartialRentAmount(String roomNumber, LocalDate checkInDate, boolean withFood) {
        try {
            Double perDayRent = roomRepo.findPerDayPrice(roomNumber);
            LocalDate lastDayOfMonth = checkInDate.with(TemporalAdjusters.lastDayOfMonth());
            long daysDifference = ChronoUnit.DAYS.between(checkInDate, lastDayOfMonth);

            return withFood? perDayRent * daysDifference:perDayRent * daysDifference-RoomConstants.WITHOUT_FOOD;
        } catch (Exception e) {
            throw new RoomException("Error Occurred while calculating Rent Amount ");
        }
    }

    @Transactional
    public void referralProcess(Roommate roommate) {
        Roommate referredRoommate = roommateRepo.findByReferralId(roommate.getReferralId());
        if (referredRoommate == null) {
            throw new RoommateException("No Roommate matches with the entered Referral ID");
        }
        if (referredRoommate.getReferralCount() > RoomConstants.MAXIMUM_REFERRALS) {
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
        if (room == null) {
            throw new RoomException("Invalid Room Details");
        }
        if (checkRoomNumberExists(room.getRoomNumber())) {
            throw new RoomException("Already this Room number : " + room.getRoomNumber() + " was taken");
        }
        if (room.getCapacity() <= RoomConstants.NULL_VALUE) {
            throw new RoomException("Total Capacity must be greater than 0. Provided : " + room.getCapacity());
        }
        if (room.getCurrentCapacity() > room.getCapacity()) {
            throw new RoomException("Current capacity cannot be more than total capacity");
        }
        if (room.getPrice() < RoomConstants.WITHOUT_FOOD) {
            throw new RoomException("Room rent should be more than 1000");
        }

        roomRepo.save(room);
        log.info("Room added successfully: {}", room.getRoomNumber());
        return "Room have been added Successfully";

    }

    @Override
    @Transactional
    public RoomDTO editRoom(int roomId, Room room) {
        if (room == null) {
            throw new RoomException("Invalid Room Details");
        }
        Room roomFromDatabase = roomRepo.findById(roomId).orElseThrow(() -> {
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
            if (room.getCapacity() < RoomConstants.NULL_VALUE && room.getCurrentCapacity()<RoomConstants.NULL_VALUE)
                throw new RoomException("Capacity cannot be less than 0");
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
        log.info("Room updated successfully with ID: {}", roomId);
        roomRepo.save(roomFromDatabase);

        return mapper.map(roomFromDatabase,RoomDTO.class);
    }

    @Override
    @Transactional
    public String deleteRoom(int roomId) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RoomException("Mentioned Room Id is not available"));
        if (!room.getRoommateList().isEmpty())
            throw new RoomException("This room is not empty to delete");

        roomRepo.delete(room);
        log.info("Room deleted successfully with Id: {}", roomId);
        return "Room deleted Successfully";
    }

    public Boolean checkRoomNumberExists(String roomNumber) {
        log.info("Checking if room number exists: {}", roomNumber);
        return roomRepo.existsByRoomNumber(roomNumber);
    }

}
