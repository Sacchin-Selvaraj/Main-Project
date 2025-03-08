package sharespace.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sharespace.model.OwnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<OwnerDetails,Integer> {

    OwnerDetails findByOwnerName(String ownerName);

    boolean existsByOwnerName(String ownerName);
}
