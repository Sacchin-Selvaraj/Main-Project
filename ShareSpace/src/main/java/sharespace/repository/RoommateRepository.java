package sharespace.repository;

import sharespace.model.Roommate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate,Integer> {
    
    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Roommate findByUsername(String username);

    Roommate findByReferralId(String referId);
}
