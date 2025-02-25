package sharespace.repository;

import sharespace.model.OwnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<OwnerDetails,Integer> {

    OwnerDetails findByOwnerName(String ownerName);
}
