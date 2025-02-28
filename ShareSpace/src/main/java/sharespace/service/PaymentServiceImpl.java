package sharespace.service;

import org.springframework.data.domain.Page;
import sharespace.exception.PaymentException;
import sharespace.exception.RoommateException;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.RentStatus;
import sharespace.model.Roommate;
import sharespace.repository.PaymentRepository;
import sharespace.repository.RoommateRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RoommateRepository roommateRepo;

    private final PaymentRepository paymentRepo;


    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    public PaymentServiceImpl(RoommateRepository roommateRepo, PaymentRepository paymentRepo) {
        this.roommateRepo = roommateRepo;
        this.paymentRepo = paymentRepo;
    }

    @Override
    public PaymentCallBackRequest createPaymentForUser(String username) throws RazorpayException {
        System.out.println(username);
        Roommate roommate = roommateRepo.findByUsername(username);

        if (roommate == null)
            throw new RoommateException("No User found under this name");

        Payment payment = new Payment();
        payment = createPayment(roommate.getRentAmount(), "INR", roommate.getUsername(), payment);
        roommate.setRentStatus(RentStatus.PAYMENT_CREATED);
        roommate.getPaymentList().add(payment); // Circular Reference
        payment.setUsername(roommate.getUsername());
        payment.setRoomNumber(roommate.getRoomNumber());
        paymentRepo.save(payment);
        roommateRepo.save(roommate);

        PaymentCallBackRequest response=new PaymentCallBackRequest();
        response.setOrderId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setEmail(roommate.getEmail());

        return response;
    }

    public Payment createPayment(double amount, String currency, String receipt, Payment payment) throws RazorpayException {

        RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        System.out.println(orderRequest.toString());
        Order order = razorpayClient.orders.create(orderRequest);

        int amountPaid = (int) order.get("amount");
        double rent = (double) amountPaid;
        payment.setTransactionId(order.get("id"));
        payment.setPaymentStatus(order.get("status"));
        payment.setAmount(rent/100);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(order.get("entity"));

        System.out.println(order.toString());

        return payment;
    }


    @Override
    public String updateStatus(PaymentCallBackRequest request) throws RazorpayException {
        String razorOrderId = request.getOrderId();
        RazorpayClient razorpayClient=new RazorpayClient(apiKey,apiSecret);
        com.razorpay.Payment paymentData =razorpayClient.payments.fetch(request.getPaymentId());
        System.out.println(paymentData.toString());
        if(!request.getPaymentId().equals(paymentData.get("id"))){
            System.out.println("Payment was not Processed with correct Order Id");
            throw new PaymentException("Payment was not Processed with correct Order Id");
        }
        Payment payment = paymentRepo.findBytransactionId(razorOrderId);
        String userName=payment.getUsername();
        Roommate roommate=roommateRepo.findByUsername(userName);
        roommate.setRentStatus(RentStatus.PAYMENT_DONE);
        roommateRepo.save(roommate);
        if (payment != null) {
            payment.setPaymentStatus(paymentData.get("status"));
            payment.setTransactionId(request.getPaymentId());
            payment.setPaymentMethod(paymentData.get("method"));
            paymentRepo.save(payment);
            return "Payment is Successful";

        }
        return "Payment not Successful";

    }

    @Override
    public List<Payment> getAllPayments() {
        List<Payment> paymentList=paymentRepo.findAll();
        if (paymentList.isEmpty())
            throw new PaymentException("No Payments have been done so far");

        return paymentList;
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepo.save(payment);
    }

    @Override
    public Page<Payment> sortPayments(Integer page, Integer limit, LocalDate paymentDate, String sortField, String sortOrder) {


        return null;
    }

    @Override
    public Payment searchUsername(String username) {
        Payment payment=paymentRepo.findByUsername(username);
        if (payment==null)
            throw new PaymentException("Entered Username was Invalid");

        return payment;
    }

}
