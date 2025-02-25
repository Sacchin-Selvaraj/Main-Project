package sharespace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sharespace.exception.PaymentException;
import sharespace.exception.RoommateException;
import sharespace.model.Payment;
import sharespace.model.Roommate;
import sharespace.repository.PaymentRepository;
import sharespace.repository.RoommateRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @Mock
    private RoommateRepository roommateRepo;

    @Mock
    private PaymentRepository paymentRepo;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void CreatePaymentForUser_UserNotFound() {
        String username = "invalidUser";

        when(roommateRepo.findByUsername(username)).thenReturn(null);

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            paymentService.createPaymentForUser(username);
        });
        assertEquals("No User found under this name", exception.getMessage());
        verify(roommateRepo, times(1)).findByUsername(username);
        verify(paymentRepo, never()).save(any(Payment.class));
        verify(roommateRepo, never()).save(any(Roommate.class));
    }

    @Test
    void GetAllPayments_Success() {
        Payment payment1 = new Payment();
        payment1.setTransactionId("order123");
        payment1.setAmount(1000.0);

        Payment payment2 = new Payment();
        payment2.setTransactionId("order456");
        payment2.setAmount(1200.0);

        List<Payment> paymentList = Arrays.asList(payment1, payment2);

        when(paymentRepo.findAll()).thenReturn(paymentList);

        List<Payment> result = paymentService.getAllPayments();

        assertEquals(2, result.size());
        assertEquals("order123", result.get(0).getTransactionId());
        assertEquals("order456", result.get(1).getTransactionId());
        verify(paymentRepo, times(1)).findAll();
    }

    @Test
    void GetAllPayments_NoPayments() {
        when(paymentRepo.findAll()).thenReturn(List.of());

        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentService.getAllPayments();
        });
        assertEquals("No Payments have been done so far", exception.getMessage());
        verify(paymentRepo, times(1)).findAll();
    }
}