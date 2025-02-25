package sharespace.controller;

import com.razorpay.RazorpayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.PaymentDetails;
import sharespace.service.PaymentService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void PayRent() throws RazorpayException {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setUsername("user1");

        PaymentCallBackRequest paymentCallBackRequest = new PaymentCallBackRequest();
        paymentCallBackRequest.setOrderId("order123");

        when(paymentService.createPaymentForUser("user1")).thenReturn(paymentCallBackRequest);

        ResponseEntity<PaymentCallBackRequest> response = paymentController.payRent(paymentDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("order123", response.getBody().getOrderId());
        verify(paymentService, times(1)).createPaymentForUser("user1");
    }

    @Test
    void PaymentCallback() throws RazorpayException {

        PaymentCallBackRequest request = new PaymentCallBackRequest();
        request.setOrderId("order123");

        when(paymentService.updateStatus(request)).thenReturn("Payment status updated successfully");

        ResponseEntity<String> response = paymentController.paymentCallback(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Payment status updated successfully", response.getBody());
        verify(paymentService, times(1)).updateStatus(request);
    }

    @Test
    void GetDetails() {

        Payment payment1 = new Payment();
        payment1.setTransactionId("order123");
        payment1.setPaymentStatus("paid");

        Payment payment2 = new Payment();
        payment2.setTransactionId("order456");
        payment2.setPaymentStatus("created");

        List<Payment> paymentList = Arrays.asList(payment1, payment2);

        when(paymentService.getAllPayments()).thenReturn(paymentList);

        ResponseEntity<List<Payment>> response = paymentController.getDetails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("paid", response.getBody().get(0).getPaymentStatus());
        assertEquals("created", response.getBody().get(1).getPaymentStatus());
        verify(paymentService, times(1)).getAllPayments();
    }
}