package sharespace.service;

import sharespace.model.Grievances;
import sharespace.payload.GrievancesDTO;

import java.util.List;

public interface GrievanceService {

    String raiseAnGrievance(int roommateId, Grievances grievances);

    List<GrievancesDTO> getPendingGrievances();

    String markPendingGrievances(int grievanceId);
}
