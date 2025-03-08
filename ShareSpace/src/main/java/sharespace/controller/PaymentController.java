package sharespace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.PaymentDetails;
import sharespace.payload.PaymentDTO;
import sharespace.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {


    private final PaymentService paymentService;
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payrent")
    public ResponseEntity<PaymentCallBackRequest> payRent(@RequestBody PaymentDetails paymentDetails) throws RazorpayException {
        logger.info("Received request to pay rent for user: {}", paymentDetails.getUsername());
        PaymentCallBackRequest payment=paymentService.createPaymentForUser(paymentDetails.getUsername());
        logger.info("Payment order created successfully for user: {}", paymentDetails.getUsername());
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/paymentCallback")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallBackRequest request) throws RazorpayException {
        logger.info("Received payment callback request with order ID: {}", request.getOrderId());
        String paymentMessage=paymentService.updateStatus(request);
        logger.info("Payment status updated successfully for order ID: {}", request.getOrderId());
        return new ResponseEntity<>(paymentMessage,HttpStatus.OK);
    }

    @GetMapping("/paymentDetails")
    public ResponseEntity<List<Payment>> getDetails(){
        logger.info("Received request to fetch all payment details");
        List<Payment> paymentList = paymentService.getAllPayments();
        logger.info("Fetched {} payment records", paymentList.size());
        return new ResponseEntity<>(paymentList, HttpStatus.OK);
    }

    @PostMapping("/add")
    public Payment addPayment(@RequestBody Payment payment){
        logger.info("Received request to add a new payment: {}", payment);
        Payment addedPayment = paymentService.addPayment(payment);
        logger.info("Payment added successfully with ID: {}", addedPayment.getId());
        return addedPayment;
    }

    @GetMapping("/sort")
    public ResponseEntity<Page<PaymentDTO>> sortPayments(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "paymentDate",required = false) LocalDate paymentDate,
            @RequestParam(name = "sortField",defaultValue = "username" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder

    ){
        logger.info("Received request to sort payments - Page: {}, Limit: {}, Payment Date: {}, Sort Field: {}, Sort Order: {}",
                page, limit, paymentDate, sortField, sortOrder);
        Page<PaymentDTO> payments=paymentService.sortPayments(page,limit,paymentDate,sortField,sortOrder);
        logger.info("Sorted {} payment records", payments.getTotalElements());
        return new ResponseEntity<>(payments,HttpStatus.OK);
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<List<Payment>> searchUser(@PathVariable String username){
        logger.info("Received request to search payments for username: {}", username);
        List<Payment> payments = paymentService.searchUsername(username);
        logger.info("Found {} payment records for username: {}", payments.size(), username);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

}

