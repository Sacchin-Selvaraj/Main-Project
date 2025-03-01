package sharespace.controller;


import sharespace.model.MailResponse;
import sharespace.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/send-mail")
    public ResponseEntity<MailResponse> sendMail(){
        MailResponse mailResponse=notificationService.sendMailToRoommate();
        return new ResponseEntity<>(mailResponse, HttpStatus.OK);
    }

    @GetMapping("/send-rentpending")
    public ResponseEntity<MailResponse> sendPaymentPendingNotification(){

        return new ResponseEntity<>(notificationService.sendPendingMail(),HttpStatus.OK);
    }

    @GetMapping("/load")
    public MailResponse sendmailAutomatically(){
        return notificationService.sendMailToRoommateAutomatically();

    }

}
