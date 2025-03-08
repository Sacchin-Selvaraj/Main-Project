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
        List<Roommate> roommateList = roommateRepo.findAll();
        if (roommateList.isEmpty()) {
            throw new RoommateException("No Roommate available");
        }
        log.info("Successfully fetched {} roommates", roommateList.size());
        return roommateList;
    }

    @Override
    public Roommate updateEmail(int id, String email) {
        if (email == null || email.isEmpty()) {
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
        Roommate existingRoommate = roommateRepo.findById(roommateId)
                .orElseThrow(() -> {
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
        String username = loginDetails.getUsername();
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null){
            throw new RoommateException("Username is invalid");
        }

        String encryptedPassword = roommate.getPassword();
        String decryptedPassword = passwordUtils.decrypt(encryptedPassword);
        if (!decryptedPassword.equals(loginDetails.getPassword())) {
            throw new RoommateException("Password was invalid");
        }

        log.info("Roommate details fetched successfully for username: {}", username);
        return roommate;
    }

    @Override
    @Transactional
    public void deleteRoommate(String username) {
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null) {
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
        if (updateDetails.getPassword() != null&&updateDetails.getPassword().length()>5) {
            String encryptedPassword = passwordUtils.encrypt(updateDetails.getPassword());
            roommate.setPassword(encryptedPassword);
        }
        if (updateDetails.getWithFood() != null && !roommate.getWithFood().equals(updateDetails.getWithFood())) {
            if (roommate.getLastModifiedDate().isAfter(LocalDate.now().minusDays(28))){
                throw new RoommateException("You can edit the Food service only after : "
                        +roommate.getLastModifiedDate().plusDays(28));
            }
            roommate.setLastModifiedDate(LocalDate.now());
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
            if (vacateRequest.getCheckOutDate().isBefore(LocalDate.now())) {
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
            throw new RoommateException("Already Vacate Request have been sent");
        }
    }

    @Override
    public List<VacateResponseDTO> getPendingVacateRequests() {
        List<VacateRequest> vacateRequests = vacateRepo.findByIsReadFalse();
        if (vacateRequests.isEmpty()){
            throw new RoommateException("No Vacate Request so Far");
        }

        modelMapper.typeMap(VacateRequest.class, VacateResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoommate().getUsername(), VacateResponseDTO::setRoommateName);
                    mapper.map(src -> src.getRoommate().getRoomNumber(), VacateResponseDTO::setRoomNumber);
                });

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
        VacateRequest vacateRequest = vacateRepo.findById(requestId)
                .orElseThrow(() -> {
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
            throw new RoommateException("No Roommates available");
        }
        log.info("Fetched {} roommates details", roommatePage.getTotalElements());
        return roommatePage;
    }

}
