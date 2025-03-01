package sharespace.controller;

import org.springframework.data.domain.Page;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.PaymentDetails;
import sharespace.model.RentStatus;
import sharespace.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sharespace.service.PaymentServiceImpl;

import java.time.LocalDate;
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

    @PostMapping("/add")
    public Payment addPayment(@RequestBody Payment payment){
        return paymentService.addPayment(payment);
    }

    @GetMapping("/sort")
    public ResponseEntity<Page<Payment>> sortPayments(
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "paymentDate",required = false) LocalDate paymentDate,
            @RequestParam(name = "sortField",defaultValue = "username" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder

    ){
        Page<Payment> payments=paymentService.sortPayments(page,limit,paymentDate,sortField,sortOrder);
        return new ResponseEntity<>(payments,HttpStatus.OK);
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<List<Payment>> searchUser(@PathVariable String username){
        List<Payment> payment=paymentService.searchUsername(username);
        return new ResponseEntity<>(payment,HttpStatus.OK);

    }

}

