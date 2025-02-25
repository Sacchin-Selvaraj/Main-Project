package sharespace.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sharespace.model.MailResponse;
import sharespace.service.NotificationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMail() {
        MailResponse mailResponse=new MailResponse();
        mailResponse.setMessage("Mail Sent Successfully");
        when(notificationService.sendMailToRoommate()).thenReturn(mailResponse);

        ResponseEntity<MailResponse> response=notificationController.sendMail();

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Mail Sent Successfully",response.getBody().getMessage());
        verify(notificationService,times(1)).sendMailToRoommate();
    }

    @Test
    void sendPaymentPendingNotification() {
        MailResponse mailResponse=new MailResponse();
        mailResponse.setMessage("Mail Sent Successfully");
        when(notificationService.sendPendingMail()).thenReturn(mailResponse);

        ResponseEntity<MailResponse> response=notificationController.sendPaymentPendingNotification();

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Mail Sent Successfully",response.getBody().getMessage());
        verify(notificationService,times(1)).sendPendingMail();

    }
}