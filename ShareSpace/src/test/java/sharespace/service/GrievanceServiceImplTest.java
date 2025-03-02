package sharespace.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import sharespace.exception.GrievanceException;
import sharespace.exception.RoommateException;
import sharespace.model.Grievances;
import sharespace.model.Roommate;
import sharespace.payload.GrievancesDTO;
import sharespace.repository.GrievanceRepository;
import sharespace.repository.RoommateRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrievanceServiceImplTest {

    @Mock
    private GrievanceRepository grievanceRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GrievanceServiceImpl grievanceServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void raiseAnGrievance_RoommateFound() {
        int roommateId = 1;
        Grievances grievance = new Grievances();
        grievance.setGrievanceContent("Issue with AC");

        Roommate roommate = new Roommate();
        roommate.setRoommateId(roommateId);
        roommate.setUsername("user1");

        when(roommateRepository.findById(roommateId)).thenReturn(Optional.of(roommate));

        String response = grievanceServiceImpl.raiseAnGrievance(roommateId, grievance);

        assertEquals("Raised an Grievance Successfully", response);
        verify(roommateRepository, times(1)).findById(roommateId);
        verify(grievanceRepository, never()).save(any());
        verify(roommateRepository, times(1)).save(roommate);

    }

    @Test
    void RaiseAnGrievance_RoommateNotFound() {
        int roommateId = 1;
        Grievances grievance = new Grievances();
        grievance.setGrievanceContent("Issue with AC");

        when(roommateRepository.findById(roommateId)).thenReturn(Optional.empty());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            grievanceServiceImpl.raiseAnGrievance(roommateId, grievance);
        });
        assertEquals("Entered Roommate id was invalid", exception.getMessage());
        verify(roommateRepository, times(1)).findById(roommateId);
        verify(grievanceRepository, never()).save(any());
        verify(roommateRepository, never()).save(any());
    }

    @Test
    void getPendingGrievances() {
        Grievances grievance1 = new Grievances();
        grievance1.setGrievanceContent("Issue with AC");
        Grievances grievance2 = new Grievances();
        grievance2.setGrievanceContent("Issue with Chair");

        List<Grievances> grievances= Arrays.asList(grievance1,grievance2);

        when(grievanceRepository.findByIsReadFalse()).thenReturn(grievances);

        GrievancesDTO grievanceDTO1 = new GrievancesDTO();
        grievanceDTO1.setGrievanceId(1);
        grievanceDTO1.setGrievanceContent("Issue with AC");
        grievanceDTO1.setRoommateName("user1");
        grievanceDTO1.setRoomNumber("101");

        GrievancesDTO grievanceDTO2 = new GrievancesDTO();
        grievanceDTO2.setGrievanceId(2);
        grievanceDTO2.setGrievanceContent("Broken Chair");
        grievanceDTO2.setRoommateName("user2");
        grievanceDTO2.setRoomNumber("102");

        when(modelMapper.map(grievance1, GrievancesDTO.class)).thenReturn(grievanceDTO1);
        when(modelMapper.map(grievance2, GrievancesDTO.class)).thenReturn(grievanceDTO2);

        TypeMap<Grievances, GrievancesDTO> typeMap = mock(TypeMap.class);
        when(modelMapper.typeMap(Grievances.class, GrievancesDTO.class)).thenReturn(typeMap);

        List<GrievancesDTO> result = grievanceServiceImpl.getPendingGrievances();

        assertEquals(2, result.size());
        assertEquals("Issue with AC", result.get(0).getGrievanceContent());
        assertEquals("Broken Chair", result.get(1).getGrievanceContent());
        verify(grievanceRepository, times(1)).findByIsReadFalse();
    }

    @Test
    void markPendingGrievances() {
        Grievances grievance=new Grievances();
        when(grievanceRepository.findById(1)).thenReturn(Optional.of(grievance));

        String response=grievanceServiceImpl.markPendingGrievances(1);

        assertEquals("Marked as Read",response);
        verify(grievanceRepository,times(1)).findById(1);
        verify(grievanceRepository, times(1)).save(grievance);
    }

    @Test
    void markPendingGrievances_GrievanceNotFound() {

        when(grievanceRepository.findById(1)).thenReturn(Optional.empty());

        GrievanceException exception=assertThrows(GrievanceException.class,()->grievanceServiceImpl.markPendingGrievances(1));

        assertEquals("Entered Grievance Id was invalid",exception.getMessage());
        verify(grievanceRepository,times(1)).findById(1);

    }

}