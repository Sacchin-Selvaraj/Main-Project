package sharespace.controller;

import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.PaymentDetails;
import sharespace.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/payments")
public class PaymentController {


    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payrent")
    public ResponseEntity<PaymentCallBackRequest> payRent(@RequestBody PaymentDetails paymentDetails) throws RazorpayException {
        System.out.println(paymentDetails.getUsername());
        PaymentCallBackRequest payment=paymentService.createPaymentForUser(paymentDetails.getUsername());
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/paymentCallback")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallBackRequest request) throws RazorpayException {
        String paymentMessage=paymentService.updateStatus(request);
        return new ResponseEntity<>(paymentMessage,HttpStatus.OK);
    }

    @GetMapping("/paymentDetails")
    public ResponseEntity<List<Payment>> getDetails(){
        List<Payment> paymentList=paymentService.getAllPayments();
        return new ResponseEntity<>(paymentList,HttpStatus.OK);

    }

}

