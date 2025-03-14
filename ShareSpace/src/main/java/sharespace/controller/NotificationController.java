package sharespace.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sharespace.model.MailResponse;
import sharespace.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    private static final Logger logger=LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/send-mail")
    public ResponseEntity<MailResponse> sendMail() throws ExecutionException, InterruptedException {
        logger.info("Received an request to send an Email to all roommates");
        CompletableFuture<MailResponse> mailResponse=notificationService.sendMailToRoommate();
        logger.info("Mail sent successfully to all roommates");
        MailResponse mailResponseIfSent=new MailResponse("Mail sent successfully",false);
        return new ResponseEntity<>(mailResponse.getNow(mailResponseIfSent), HttpStatus.OK);
    }

    @GetMapping("/send-rent-pending")
    public ResponseEntity<MailResponse> sendPaymentPendingNotification(){
        logger.info("received an request to sent mail to payment pending roommates");
        return new ResponseEntity<>(notificationService.sendPendingMail(),HttpStatus.OK);
    }

    @GetMapping("/load")
    public MailResponse sendmailAutomatically(){
        logger.info("Automatically triggered the mail");
        return notificationService.sendMailToRoommateAutomatically();

    }

}
