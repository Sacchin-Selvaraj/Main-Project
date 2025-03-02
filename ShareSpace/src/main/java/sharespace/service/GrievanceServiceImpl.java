package sharespace.service;

import jakarta.transaction.Transactional;
import sharespace.exception.GrievanceException;
import sharespace.exception.RoommateException;
import sharespace.model.Grievances;
import sharespace.model.Roommate;
import sharespace.payload.GrievancesDTO;
import sharespace.repository.GrievanceRepository;
import sharespace.repository.RoommateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GrievanceServiceImpl implements GrievanceService {

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
        if(grievance==null)
            throw new GrievanceException("Invalid data in Grievance");
        Roommate roommate=roommateRepo.findById(roommateId).orElseThrow(() -> new RoommateException("Entered Roommate id was invalid"));
        grievance.setCreatedAt(LocalDate.now());
        grievance.setIsRead(false);
        grievance.setRoommate(roommate);
        roommate.getGrievances().add(grievance);
        roommateRepo.save(roommate);
        return "Raised an Grievance Successfully";
    }

    @Override
    @Transactional
    public List<GrievancesDTO> getPendingGrievances() {
        List<Grievances> grievances=grievanceRepo.findByIsReadFalse();

        if (grievances.isEmpty())
            throw new GrievanceException("No Grievance so Far");

        modelMapper.typeMap(Grievances.class,GrievancesDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getRoommate().getUsername(), GrievancesDTO::setRoommateName);
                    mapper.map(src -> src.getRoommate().getRoomNumber(), GrievancesDTO::setRoomNumber);
                });

        List<GrievancesDTO> grievancesDTOS=grievances.stream().map(grievances1 -> {
            GrievancesDTO grievancesDTO=modelMapper.map(grievances1, GrievancesDTO.class);
            grievancesDTO.setCreatedAt(LocalDate.now());
            return grievancesDTO;
        }).toList();

        return grievancesDTOS;
    }

    @Override
    @Transactional
    public String markPendingGrievances(int grievanceId) {
        Grievances grievance=grievanceRepo.findById(grievanceId).orElseThrow(() -> new GrievanceException("Entered Grievance Id was invalid"));
        grievance.setIsRead(true);
        grievanceRepo.save(grievance);
        return "Marked as Read";
    }
}
