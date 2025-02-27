package sharespace.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sharespace.model.RentStatus;
import sharespace.model.Roommate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate,Integer>, PagingAndSortingRepository<Roommate,Integer> {
    
    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Roommate findByUsername(String username);

    Roommate findByReferralId(String referId);

    @Query("SELECT r from Roommate r where r.rentStatus = :rentStatus")
    Page<Roommate> findByRentStatus(@Param("rentStatus") RentStatus rentStatus, Pageable pageable);
}
