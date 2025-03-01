package sharespace.service;

import org.springframework.data.domain.Page;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import com.razorpay.RazorpayException;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {
    PaymentCallBackRequest createPaymentForUser(String username) throws RazorpayException;

    String  updateStatus(PaymentCallBackRequest request) throws RazorpayException;

    List<Payment> getAllPayments();

    Payment addPayment(Payment payment);

    Page<Payment> sortPayments(Integer page, Integer limit, LocalDate paymentDate, String sortField, String sortOrder);

    List<Payment> searchUsername(String username);
}
