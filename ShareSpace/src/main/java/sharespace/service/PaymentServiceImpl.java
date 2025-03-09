package sharespace.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sharespace.exception.PaymentException;
import sharespace.exception.RoommateException;
import sharespace.model.Payment;
import sharespace.model.PaymentCallBackRequest;
import sharespace.model.RentStatus;
import sharespace.model.Roommate;
import sharespace.payload.PaymentDTO;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final RoommateRepository roommateRepo;
    private final PaymentRepository paymentRepo;
    private final ModelMapper mapper;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    public PaymentServiceImpl(RoommateRepository roommateRepo, PaymentRepository paymentRepo, ModelMapper mapper) {
        this.roommateRepo = roommateRepo;
        this.paymentRepo = paymentRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PaymentCallBackRequest createPaymentForUser(String username) throws RazorpayException {
        if (username == null || username.isEmpty()) {
            throw new RoommateException("Username cannot be null or empty");
        }
        Roommate roommate = roommateRepo.findByUsername(username);
        if (roommate == null) {
            throw new RoommateException("No User found under this name : " + username);
        }
        Payment payment = new Payment();
        payment = createPayment(roommate.getRentAmount(), "INR", roommate.getUsername(), payment);
        roommate.setRentStatus(RentStatus.PAYMENT_CREATED);
        roommate.getPaymentList().add(payment);
        payment.setUsername(roommate.getUsername());
        payment.setRoomNumber(roommate.getRoomNumber());
        payment.setRoommate(roommate);
        roommateRepo.save(roommate);

        log.info("Payment created successfully for user: {}", username);

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
        Order order = razorpayClient.orders.create(orderRequest);

        int amountPaid = (int) order.get("amount");
        payment.setTransactionId(order.get("id"));
        payment.setPaymentStatus("PAYMENT_FAILED");
        payment.setAmount((double) amountPaid /100);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(order.get("entity"));

        log.info("Payment created with orderId: {}", payment.getTransactionId());
        return payment;
    }


    @Override
    @Transactional
    public String updateStatus(PaymentCallBackRequest request) throws RazorpayException {
        String razorOrderId = request.getOrderId();
        RazorpayClient razorpayClient=new RazorpayClient(apiKey,apiSecret);
        com.razorpay.Payment paymentData =razorpayClient.payments.fetch(request.getPaymentId());
        if(!request.getPaymentId().equals(paymentData.get("id"))){
            throw new PaymentException("Payment was not Processed with correct Order Id");
        }
        Payment payment = paymentRepo.findBytransactionId(razorOrderId);
        if (payment == null) {
            throw new PaymentException("Payment not found");
        }
        String userName=payment.getUsername();
        Roommate roommate=roommateRepo.findByUsername(userName);
        roommate.setRentStatus(RentStatus.PAYMENT_DONE);
        roommateRepo.save(roommate);

        payment.setPaymentStatus("PAYMENT_DONE");
        payment.setTransactionId(request.getPaymentId());
        payment.setPaymentMethod(paymentData.get("method"));
        paymentRepo.save(payment);

        log.info("Payment status updated successfully for order Id: {}", request.getOrderId());
        return "Payment not Successful";

    }

    @Override
    public List<Payment> getAllPayments() {
        List<Payment> paymentList = paymentRepo.findAll();
        if (paymentList.isEmpty()) {
            throw new PaymentException("No Payments have been done so far");
        }

        log.info("Fetched {} payments", paymentList.size());
        return paymentList;
    }

    @Override
    public Payment addPayment(Payment payment) {
        Payment savedPayment = paymentRepo.save(payment);
        log.info("Payment added successfully with transaction ID: {}", savedPayment.getTransactionId());
        return savedPayment;
    }

    @Override
    @Transactional
    public Page<PaymentDTO> sortPayments(Integer page, Integer limit, LocalDate paymentDate, String sortField, String sortOrder) {
        Sort sort=sortOrder.equalsIgnoreCase("asc")?Sort.by(sortField).ascending():Sort.by(sortField).descending();

        Pageable pageable= PageRequest.of(page,limit,sort);
        Page<Payment> paymentPage;

        if (paymentDate==null){
            paymentPage=paymentRepo.findAll(pageable);
        }else {
            paymentPage=paymentRepo.findByPaymentDate(paymentDate,pageable);
        }
        if (paymentPage.isEmpty()) {
            throw new PaymentException("No Payment available ");
        }

        log.info("Fetched {} payment Details", paymentPage.getTotalElements());

        return paymentPage.map(payment -> {
            PaymentDTO paymentDTO = mapper.map(payment, PaymentDTO.class);
            paymentDTO.setUsername(payment.getRoommate().getUsername());
            paymentDTO.setRoomNumber(payment.getRoommate().getRoomNumber());
            return paymentDTO;
        });
    }

    @Override
    public List<PaymentDTO> searchUsername(String username) {
        List<Payment> paymentList;
        if (username.length()<3){
            paymentList=paymentRepo.findAllByRoomNumber(username);
        }else {
            paymentList=paymentRepo.findAllByUsername(username);
        }
        if (paymentList.isEmpty()) {
            throw new PaymentException("No Payments available under - " + username);
        }
        log.info("Fetched {} payments for username: {}", paymentList.size(), username);

        return paymentList.stream().map(payment ->{
            PaymentDTO paymentDTO=mapper.map(payment,PaymentDTO.class);
            paymentDTO.setUsername(payment.getRoommate().getUsername());
            paymentDTO.setRoomNumber(payment.getRoommate().getRoomNumber());
            return paymentDTO;
        }).toList();
    }

}
