package sharespace.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import sharespace.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {

    @Query("SELECT p FROM Payment p WHERE transactionId=?1 ")
    Payment findBytransactionId(String razorpayId);

    Payment findByUsername(String username);

    @Query("SELECT p from Payment p where p.paymentDate=:paymentDate")
    Page<Payment> findByPaymentDate(@Param("paymentDate") LocalDate paymentDate, Pageable pageable);

    List<Payment> findAllByRoomNumber(String username);

    List<Payment> findAllByUsername(String username);

    @Modifying
    @Query("UPDATE Payment SET username = :newUsername WHERE username= :oldUsername")
    void updateUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);
}
