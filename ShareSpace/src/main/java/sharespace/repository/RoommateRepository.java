package sharespace.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import sharespace.model.Roommate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate,Integer>, PagingAndSortingRepository<Roommate,Integer> {
    
    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Roommate findByUsername(String username);

    Roommate findByReferralId(String referId);

    Page<Roommate> findByRentStatus(String rentStatus, Pageable pageable);
}
