package sharespace.repository;

import sharespace.model.VacateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacateRepository extends JpaRepository<VacateRequest,Integer> {

    List<VacateRequest> findByIsReadFalse();

}
