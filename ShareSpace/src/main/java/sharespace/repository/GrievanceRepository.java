package sharespace.repository;

import sharespace.model.Grievances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievances,Integer> {
    List<Grievances> findByIsReadFalse();
}
