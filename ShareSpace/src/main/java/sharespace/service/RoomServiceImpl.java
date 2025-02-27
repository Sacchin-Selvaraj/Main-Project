package sharespace.service;

import sharespace.exception.RoomException;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.password.PasswordUtils;
import sharespace.payload.RoommateDTO;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepo;

    private final RoommateRepository roommateRepo;

    private final ModelMapper mapper;

    private final PasswordUtils passwordUtils;


    private static final int MAXIMUM_REFERRALS=2;

    private static final double REFERRAL_PERCENTAGE=0.05;

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
        int capacity=0;
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

        if (Objects.equals(room.getCapacity(), room.getCurrentCapacity()))
            throw new RoomException("Room was Full");

        checkUsername(roommate);// check for the Username and Email is already present or not

        String encrpytedPassword=passwordUtils.encrypt(roommate.getPassword());
        roommate.setPassword(encrpytedPassword);

        if(roommate.getReferralId()!=null&&roommate.getReferralId().length()>5)
            referralProcess(roommate);

        if (roommate.getWithFood()) {
            roommate.setRentAmount(room.getPrice());
        } else {
            roommate.setRentAmount(room.getPrice() - 500);
        }
        roommate.setCheckInDate(LocalDate.now());
        roommate.setReferralId(generateReferId(roommate.getUsername()));
        roommate.setReferralCount(0);
        roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
        roommate.setRoomNumber(room.getRoomNumber());
        room.getRoommateList().add(roommate);
        room.setCurrentCapacity(room.getCurrentCapacity() + 1);
        roomRepo.save(room);
        RoommateDTO roommateDTO = mapper.map(roommate, RoommateDTO.class);

        return roommateDTO;

    }

    public void referralProcess(Roommate roommate) {

        String referralId = roommate.getReferralId();
        Roommate referredRoommate=roommateRepo.findByReferralId(referralId);
        if(referredRoommate==null)
            throw new RoommateException("No Roommate matches with the entered Referral ID");
        if (referredRoommate.getReferralCount()>MAXIMUM_REFERRALS)
            throw new RoommateException("Already "+referredRoommate.getUsername()+" have reached max referrals");

        double rentAmount= calculateDiscount(referredRoommate.getReferralCount()+1,referredRoommate);
        System.out.println(rentAmount);
        referredRoommate.setRentAmount(rentAmount);
        referredRoommate.setReferralCount(referredRoommate.getReferralCount()+1);

        ReferralDetails referralDetails=new ReferralDetails();
        referralDetails.setUsername(roommate.getUsername());
        referralDetails.setReferralDate(LocalDate.now());

        referredRoommate.getReferralDetailsList().add(referralDetails);
        roommateRepo.save(referredRoommate);

    }

    private double calculateDiscount(int referCount,Roommate referredRoommate) {
        if (referCount == 1) return referredRoommate.getRentAmount()-(referredRoommate.getRentAmount()*(REFERRAL_PERCENTAGE*referCount));
        if (referCount == 2) return referredRoommate.getRentAmount()-(referredRoommate.getRentAmount()*(REFERRAL_PERCENTAGE*referCount));
        if (referCount == 3) return referredRoommate.getRentAmount()-(referredRoommate.getRentAmount()*(REFERRAL_PERCENTAGE*referCount));
        return referredRoommate.getRentAmount();
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
    public String addRooms(Room room) {
        if (room==null)
            throw new RoomException("Invalid Room Details");

        if (checkRoomNumberExists(room.getRoomNumber()))
            throw new RoomException("Already this room number exists");

        roomRepo.save(room);
        return "Room have been added Successfully";
    }

    @Override
    public Room editRoom(int roomId,Room room) {
        if (room==null)
            throw new RoomException("Invalid Room Details");
        Room roomFromDatabase=roomRepo.findById(roomId).orElseThrow(() -> new RoomException("No Room found under this "+roomId+" Id"));

        if (room.getRoomNumber()!=null){
            if (checkRoomNumberExists(room.getRoomNumber()))
                throw new RoomException("Already this room number exists");
            roomFromDatabase.setRoomNumber(room.getRoomNumber());
        }
        if (room.getRoomType()!=null){
            roomFromDatabase.setRoomType(room.getRoomType());
        }
        if (room.getPrice()!=null){
            roomFromDatabase.setPrice(room.getPrice());
        }
        if(room.getFloorNumber()!=null){
            roomFromDatabase.setFloorNumber(room.getFloorNumber());
        }
        if (room.getCapacity()!=null){
            roomFromDatabase.setCapacity(room.getCapacity());
        }
        if (room.getCurrentCapacity()!=null){
            if (room.getCurrentCapacity() > roomFromDatabase.getCapacity()) {
                throw new RoomException("Current capacity cannot exceed total capacity");
            }
            roomFromDatabase.setCurrentCapacity(room.getCurrentCapacity());
        }
        if (room.getIsAcAvailable()!=null){
            roomFromDatabase.setIsAcAvailable(room.getIsAcAvailable());
        }

        return roomRepo.save(roomFromDatabase);
    }

    public Boolean checkRoomNumberExists(String roomNumber){
       return roomRepo.existsByRoomNumber(roomNumber);
    }

}
