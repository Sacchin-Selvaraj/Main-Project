package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import sharespace.exception.RoomException;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.password.PasswordUtils;
import sharespace.payload.VacateResponseDTO;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;
import sharespace.repository.VacateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoommateServiceImpl implements RoommateService {


    private final RoommateRepository roommateRepo;

    private final RoomRepository roomRepo;

    private final PasswordUtils passwordUtils;

    private final VacateRepository vacateRepo;

    private final ModelMapper modelMapper;

    public RoommateServiceImpl(RoommateRepository roommateRepo, RoomRepository roomRepo, PasswordUtils passwordUtils, VacateRepository vacateRepo, ModelMapper modelMapper) {
        this.roommateRepo = roommateRepo;
        this.roomRepo = roomRepo;
        this.passwordUtils = passwordUtils;
        this.vacateRepo = vacateRepo;
        this.modelMapper = modelMapper;
    }


    @Override
    public List<Roommate> getAllRoommates() {
        List<Roommate> roommateList = roommateRepo.findAll();
        if (roommateList.isEmpty())
            throw new RoommateException("No Roommate available");

        return roommateList;
    }

    @Override
    public Roommate updateEmail(int id, String email) {
        Roommate roommate = roommateRepo.findById(id)
                .orElseThrow(() -> new RoommateException("Roommate not found with id " + id));
        roommate.setEmail(email);
        return roommateRepo.save(roommate);
    }


    @Transactional
    public Roommate updateRoommate(int id, Roommate roommate) {
        Roommate existingRoommate = roommateRepo.findById(id)
                .orElseThrow(() -> new RoommateException("Roommate not found with id " + id));

        existingRoommate.setUsername(roommate.getUsername());
        existingRoommate.setPassword(roommate.getPassword());
        existingRoommate.setGender(roommate.getGender());
        existingRoommate.setRentAmount(roommate.getRentAmount());
        existingRoommate.setWithFood(roommate.getWithFood());
        existingRoommate.setCheckInDate(roommate.getCheckInDate());
        existingRoommate.setReferralId(roommate.getReferralId());
        existingRoommate.setReferralCount(roommate.getReferralCount());
        existingRoommate.setRoomNumber(roommate.getRoomNumber());

        return roommateRepo.save(existingRoommate);
    }

    @Override
    public Roommate getRoommate(LoginDetails loginDetails) {

        String username = loginDetails.getUsername();
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null)
            throw new RoommateException("Username is invalid");

        String encryptedPassword = roommate.getPassword();
        String decryptedPassword = passwordUtils.decrypt(encryptedPassword);
        if (!decryptedPassword.equals(loginDetails.getPassword()))
            throw new RoommateException("Password was invalid");

        return roommate;
    }

    @Override
    @Transactional
    public void deleteRoommate(String username) {
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null)
            throw new RoommateException("No Roommate present under this Username");
        String roomNumber = roommate.getRoomNumber();

        Room room = roomRepo.findByRoomNumber(roomNumber);
        System.out.println(room.getRoomNumber());
        if (room == null)
            throw new RoomException("Entered RoomNumber is invalid");

        room.setCurrentCapacity(room.getCurrentCapacity() - 1);
        room.getRoommateList().remove(roommate);
        roomRepo.save(room);
        roommateRepo.delete(roommate);

    }

    @Override
    @Transactional
    public Roommate updateDetails(int roommateId, UpdateDetails updateDetails) {
        Roommate roommate = roommateRepo.findById(roommateId).orElseThrow(() -> new RoommateException("No Roommate found under this Id"));
        if (updateDetails.getUsername() != null && !updateDetails.getUsername().equals(roommate.getUsername())) {
            if (checkUsernameExists(updateDetails.getUsername())) {
                throw new RoommateException("Username already exists");
            }
            roommate.setUsername(updateDetails.getUsername());
        }
        if (updateDetails.getEmail() != null && !updateDetails.getEmail().equals(roommate.getEmail())) {
            if (checkEmailIdExists(updateDetails.getEmail())) {
                throw new RoommateException("Email already exists");
            }
            roommate.setEmail(updateDetails.getEmail());
        }
        if (updateDetails.getPassword() != null) {
            String encryptedPassword = passwordUtils.encrypt(updateDetails.getPassword());
            roommate.setPassword(encryptedPassword);
        }
        if (updateDetails.getWithFood() != null && !roommate.getWithFood().equals(updateDetails.getWithFood())) {
            roommate.setWithFood(updateDetails.getWithFood());
            int rentAdjustment = updateDetails.getWithFood() ? 1000 : -1000;
            roommate.setRentAmount(roommate.getRentAmount() + rentAdjustment);
        }

        if (updateDetails.getCheckOutDate() != null) {
            roommate.setCheckOutDate(updateDetails.getCheckOutDate());
        }

        return roommateRepo.save(roommate);
    }

    private boolean checkUsernameExists(String username) {
        List<Roommate> roommateList = roommateRepo.findAll();
        boolean flag = roommateList.stream().noneMatch(roommate -> roommate.getUsername().equalsIgnoreCase(username));
        return !flag;
    }

    private boolean checkEmailIdExists(String email) {
        return roommateRepo.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public String sendVacateRequest(int roommateId, VacateRequest vacateRequest) {
        try {
            Roommate roommate = roommateRepo.findById(roommateId).orElseThrow(() -> new RoommateException("No Roommate found under this Id"));
            vacateRequest.setRoommate(roommate);
            if (vacateRequest.getCheckOutDate().isBefore(LocalDate.now()))
                throw new RoommateException("CheckOut Date can't be in Past ");
            roommate.setCheckOutDate(vacateRequest.getCheckOutDate());
            vacateRequest.setIsRead(false);
            vacateRepo.save(vacateRequest);
            roommateRepo.save(roommate);
            return "Vacate Request Sent Successfully";
        } catch (RoommateException e) {
            throw e;
        } catch (Exception e) {
            throw new RoommateException("Already Vacate Request have been sent");
        }
    }

    @Override
    public List<VacateResponseDTO> getPendingVacateRequests() {

        List<VacateRequest> vacateRequests = vacateRepo.findByIsReadFalse();
        if (vacateRequests.isEmpty())
            throw new RoommateException("No Vacate Request so Far");

        modelMapper.typeMap(VacateRequest.class, VacateResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoommate().getUsername(), VacateResponseDTO::setRoommateName);
                    mapper.map(src -> src.getRoommate().getRoomNumber(), VacateResponseDTO::setRoomNumber);
                });

        // Map VacateRequest to VacateResponseDTO using modelMapper
        List<VacateResponseDTO> vacateResponseDTOS = vacateRequests.stream()
                .map(vacateRequest -> {
                    // Map the VacateRequest to VacateResponseDTO
                    VacateResponseDTO dto = modelMapper.map(vacateRequest, VacateResponseDTO.class);

                    // Set the createdAt field to the current date and time
                    dto.setCreatedAt(LocalDateTime.now());

                    // Return the DTO
                    return dto;
                })
                .toList();

        return vacateResponseDTOS;
    }

    @Override
    public void markAsRead(int requestId) {
        VacateRequest vacateRequest = vacateRepo.findById(requestId)
                .orElseThrow(() -> new RoommateException("Vacate request not found"));
        vacateRepo.delete(vacateRequest);
    }

}
