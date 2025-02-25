package sharespace.repository;

import sharespace.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {

    @Query("SELECT p FROM Payment p WHERE transactionId=?1 ")
    Payment findBytransactionId(String razorpayId);
}
