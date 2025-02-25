package sharespace.service;

import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import com.razorpay.RazorpayException;

import java.util.List;

public interface PaymentService {
    PaymentCallBackRequest createPaymentForUser(String username) throws RazorpayException;

    String  updateStatus(PaymentCallBackRequest request) throws RazorpayException;

    List<Payment> getAllPayments();
}
