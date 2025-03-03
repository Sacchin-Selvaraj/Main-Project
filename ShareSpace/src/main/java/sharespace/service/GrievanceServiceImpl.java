package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sharespace.exception.GrievanceException;
import sharespace.exception.RoommateException;
import sharespace.model.Grievances;
import sharespace.model.Roommate;
import sharespace.payload.GrievancesDTO;
import sharespace.repository.GrievanceRepository;
import sharespace.repository.RoommateRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class GrievanceServiceImpl implements GrievanceService {

    private final static Logger log = LoggerFactory.getLogger(GrievanceServiceImpl.class);

    private final GrievanceRepository grievanceRepo;
    private final RoommateRepository roommateRepo;
    private final ModelMapper modelMapper;

    public GrievanceServiceImpl(GrievanceRepository grievanceRepo, RoommateRepository roommateRepo, ModelMapper modelMapper) {
        this.grievanceRepo = grievanceRepo;
        this.roommateRepo = roommateRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public String raiseAnGrievance(int roommateId, Grievances grievance) {
        log.info("Raise an grievance from roommate : {}", roommateId);
        if (grievance == null) {
            log.error("Grievance was null");
            throw new GrievanceException("Invalid data in Grievance");
        }
        Roommate roommate = roommateRepo.findById(roommateId).orElseThrow(() -> {
            log.error("Entered Roommate id : {} was not found", roommateId);
            return new RoommateException("Entered Roommate id was invalid");
        });
        grievance.setCreatedAt(LocalDate.now());
        grievance.setIsRead(false);
        grievance.setRoommate(roommate);
        roommate.getGrievances().add(grievance);
        log.info("Raised an Grievance Successfully with roommate Id : {}", roommateId);
        roommateRepo.save(roommate);
        return "Raised an Grievance Successfully";
    }

    @Override
    @Transactional
    public List<GrievancesDTO> getPendingGrievances() {
        log.info("Get pending grievances");
        List<Grievances> grievances = grievanceRepo.findByIsReadFalse();

        if (grievances.isEmpty()) {
            log.warn("No Grievance raised so Far");
            throw new GrievanceException("No Grievances so Far");
        }
        modelMapper.typeMap(Grievances.class, GrievancesDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoommate().getUsername(), GrievancesDTO::setRoommateName);
                    mapper.map(src -> src.getRoommate().getRoomNumber(), GrievancesDTO::setRoomNumber);
                });

        log.info("Successfully fetched pending grievance");
        return grievances.stream().map(grievances1 -> {
            GrievancesDTO grievancesDTO = modelMapper.map(grievances1, GrievancesDTO.class);
            grievancesDTO.setCreatedAt(LocalDate.now());
            return grievancesDTO;
        }).toList();
    }

    @Override
    @Transactional
    public String markPendingGrievances(int grievanceId) {
        log.info("Search grievance by grievanceId : {}", grievanceId);
        Grievances grievance = grievanceRepo.findById(grievanceId).orElseThrow(() -> {
            log.error("Entered Grievance Id : {} was invalid", grievanceId);
            return new GrievanceException("Entered Grievance Id was invalid");
        });
        grievance.setIsRead(true);
        grievanceRepo.save(grievance);
        log.info("Successfully marked grievance as read");
        return "Marked as Read";
    }
}
