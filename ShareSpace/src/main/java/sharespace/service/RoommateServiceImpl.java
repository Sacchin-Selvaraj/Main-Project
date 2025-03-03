package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sharespace.constants.RoomConstants;
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

    private static final Logger log = LoggerFactory.getLogger(RoommateServiceImpl.class);

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
        log.info("Fetching all roommates");
        List<Roommate> roommateList = roommateRepo.findAll();
        if (roommateList.isEmpty()) {
            log.error("No roommates found in the DB");
            throw new RoommateException("No Roommate available");
        }
        log.info("Successfully fetched {} roommates", roommateList.size());
        return roommateList;
    }

    @Override
    public Roommate updateEmail(int id, String email) {
        log.info("Updating email for roommate with Id: {}", id);
        if (email == null || email.isEmpty()) {
            log.error("Email cannot be null or empty");
            throw new RoommateException("Email cannot be null or empty");
        }
        Roommate roommate = roommateRepo.findById(id)
                .orElseThrow(() -> new RoommateException("Roommate not found with id " + id));
        roommate.setEmail(email);
        log.info("Email updated successfully for roommate with Id: {}", id);
        return roommateRepo.save(roommate);
    }

    @Transactional
    public Roommate updateRoommate(int roommateId, Roommate roommate) {
        log.info("Updating roommate details for ID: {}", roommateId);
        Roommate existingRoommate = roommateRepo.findById(roommateId)
                .orElseThrow(() -> {
                    log.error("Roommate not found with Id: {}", roommateId);
                    return new RoommateException("Roommate not found with id " + roommateId);
                });

        existingRoommate.setUsername(roommate.getUsername());
        existingRoommate.setPassword(roommate.getPassword());
        existingRoommate.setGender(roommate.getGender());
        existingRoommate.setRentAmount(roommate.getRentAmount());
        existingRoommate.setWithFood(roommate.getWithFood());
        existingRoommate.setCheckInDate(roommate.getCheckInDate());
        existingRoommate.setReferralId(roommate.getReferralId());
        existingRoommate.setReferralCount(roommate.getReferralCount());
        existingRoommate.setRoomNumber(roommate.getRoomNumber());

        log.info("Roommate details updated successfully for Id: {}",roommateId);
        return roommateRepo.save(existingRoommate);
    }

    @Override
    public Roommate getRoommate(LoginDetails loginDetails) {
        log.info("{} is trying to login",loginDetails.getUsername());
        String username = loginDetails.getUsername();
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null){
            log.error("Invalid username: {}", username);
            throw new RoommateException("Username is invalid");
        }

        String encryptedPassword = roommate.getPassword();
        String decryptedPassword = passwordUtils.decrypt(encryptedPassword);
        if (!decryptedPassword.equals(loginDetails.getPassword())) {
            log.error("Invalid password for username: {}", username);
            throw new RoommateException("Password was invalid");
        }

        log.info("Roommate details fetched successfully for username: {}", username);
        return roommate;
    }

    @Override
    @Transactional
    public void deleteRoommate(String username) {
        log.info("Deleting roommate with username: {}", username);
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null) {
            log.error("Roommate not found with username: {}", username);
            throw new RoommateException("No Roommate present under this Username");
        }
        String roomNumber = roommate.getRoomNumber();

        Room room = roomRepo.findByRoomNumber(roomNumber);

        room.setCurrentCapacity(room.getCurrentCapacity() - 1);
        room.getRoommateList().remove(roommate);
        roomRepo.save(room);
        roommateRepo.delete(roommate);
        log.info("Roommate details deleted successfully with username: {}", username);
    }

    @Override
    @Transactional
    public Roommate updateDetails(int roommateId, UpdateDetails updateDetails) {
        log.info("Updating details for roommate with Id: {}", roommateId);
        Roommate roommate = roommateRepo.findById(roommateId).orElseThrow(() -> new RoommateException("No Roommate found under this Id"));
        if (updateDetails.getUsername() != null && !updateDetails.getUsername().equals(roommate.getUsername())) {
            if (checkUsernameExists(updateDetails.getUsername())) {
                log.error("Username already exists: {}", updateDetails.getUsername());
                throw new RoommateException("Username already exists");
            }
            roommate.setUsername(updateDetails.getUsername());
        }
        if (updateDetails.getEmail() != null && !updateDetails.getEmail().equals(roommate.getEmail())) {
            if (checkEmailIdExists(updateDetails.getEmail())) {
                log.error("Email already exists: {}", updateDetails.getEmail());
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
            int rentAdjustment = updateDetails.getWithFood() ? RoomConstants.WITHOUT_FOOD : -(RoomConstants.WITHOUT_FOOD);
            roommate.setRentAmount(roommate.getRentAmount() + rentAdjustment);
        }

        if (updateDetails.getCheckOutDate() != null) {
            roommate.setCheckOutDate(updateDetails.getCheckOutDate());
        }
        log.info("Details updated successfully for roommate with Id: {}", roommateId);
        return roommateRepo.save(roommate);
    }

    private boolean checkUsernameExists(String username) {
        log.info("Checking if username exists: {}", username);
        List<Roommate> roommateList = roommateRepo.findAll();
        boolean flag = roommateList.stream().noneMatch(roommate -> roommate.getUsername().equalsIgnoreCase(username));
        return !flag;
    }

    private boolean checkEmailIdExists(String email) {
        log.info("Checking if email exists: {}", email);
        return roommateRepo.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public String sendVacateRequest(int roommateId, VacateRequest vacateRequest) {
        log.info("Sending vacate request for roommate with Id: {}", roommateId);
        try {
            Roommate roommate = roommateRepo.findById(roommateId).orElseThrow(() -> new RoommateException("No Roommate found under this Id"));
            vacateRequest.setRoommate(roommate);
            if (vacateRequest.getCheckOutDate().isBefore(LocalDate.now())) {
                log.error("CheckOut date cannot be in the past: {}", vacateRequest.getCheckOutDate());
                throw new RoommateException("CheckOut Date can't be in Past :" +vacateRequest.getCheckOutDate());
            }
            roommate.setCheckOutDate(vacateRequest.getCheckOutDate());
            vacateRequest.setIsRead(false);
            vacateRepo.save(vacateRequest);
            roommateRepo.save(roommate);
            log.info("Vacate request sent successfully for roommate with Id: {}", roommateId);
            return "Vacate Request Sent Successfully";
        } catch (RoommateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending vacate request: {}", e.getMessage());
            throw new RoommateException("Already Vacate Request have been sent");
        }
    }

    @Override
    public List<VacateResponseDTO> getPendingVacateRequests() {
        log.info("Fetching pending vacate requests");
        List<VacateRequest> vacateRequests = vacateRepo.findByIsReadFalse();
        if (vacateRequests.isEmpty()){
            log.warn("No pending vacate requests found");
            throw new RoommateException("No Vacate Request so Far");
        }

        modelMapper.typeMap(VacateRequest.class, VacateResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoommate().getUsername(), VacateResponseDTO::setRoommateName);
                    mapper.map(src -> src.getRoommate().getRoomNumber(), VacateResponseDTO::setRoomNumber);
                });

        log.info("Fetched pending vacate requests");
        return vacateRequests.stream()
                .map(vacateRequest -> {
                    VacateResponseDTO dto = modelMapper.map(vacateRequest, VacateResponseDTO.class);
                    dto.setCreatedAt(LocalDateTime.now());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(int requestId) {
        log.info("Marking vacate request as read with Id: {}", requestId);
        VacateRequest vacateRequest = vacateRepo.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Vacate request not found with Id: {}", requestId);
                    return new RoommateException("Vacate request not found");
                });
        vacateRepo.delete(vacateRequest);
        log.info("Vacate request marked as read and deleted with Id: {}", requestId);
    }

    @Override
    public Page<Roommate> sortRoommates(Integer pageNumber, Integer pageSize, RentStatus rentStatus, String sortField, String sortOrder) {
        Sort sort= sortOrder.equalsIgnoreCase("asc")?Sort.by(sortField).ascending():Sort.by(sortField).descending();
        Pageable pageable= PageRequest.of(pageNumber,pageSize,sort);

        Page<Roommate> roommatePage;
        if(rentStatus==null){
            roommatePage=roommateRepo.findAll(pageable);
        }else {
            roommatePage=roommateRepo.findByRentStatus(rentStatus,pageable);
        }
        if (roommatePage.isEmpty()) {
            log.warn("No roommates found with the given criteria");
            throw new RoommateException("No Roommates available");
        }
        log.info("Fetched {} roommates details", roommatePage.getTotalElements());
        return roommatePage;
    }

}
