package sharespace.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sharespace.exception.RoommateException;
import sharespace.model.MailResponse;
import sharespace.model.RentStatus;
import sharespace.model.Roommate;
import sharespace.repository.RoommateRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private RoommateRepository roommateRepo;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private NotificationServiceImpl notificationService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void SendMailToRoommate_Success() {
        Roommate roommate1 = new Roommate();
        roommate1.setUsername("user1");
        roommate1.setEmail("user1@example.com");
        roommate1.setRentAmount(1000.0);

        Roommate roommate2 = new Roommate();
        roommate2.setUsername("user2");
        roommate2.setEmail("user2@example.com");
        roommate2.setRentAmount(1200.0);

        List<Roommate> roommates = Arrays.asList(roommate1, roommate2);
        notificationService.setFromMail("noreply@sharespace.com");

        when(roommateRepo.findAll()).thenReturn(roommates);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(eq("email-template"), any(Context.class))).thenReturn("<html>Email Content</html>");

        MailResponse response = notificationService.sendMailToRoommate();

        assertEquals("Mail sent successfully", response.getMessage());
        verify(roommateRepo, times(1)).findAll();
        verify(javaMailSender, times(2)).send(mimeMessage);
    }

    @Test
    void SendMailToRoommate_NoRoommates() {
        when(roommateRepo.findAll()).thenReturn(List.of());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            notificationService.sendMailToRoommate();
        });
        assertEquals("No Roommates details present", exception.getMessage());
        verify(roommateRepo, times(1)).findAll();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void SendPendingMail_Success() {
        Roommate roommate1 = new Roommate();
        roommate1.setUsername("user1");
        roommate1.setEmail("user1@example.com");
        roommate1.setRentAmount(1000.0);
        roommate1.setRentStatus(RentStatus.PAYMENT_PENDING);

        Roommate roommate2 = new Roommate();
        roommate2.setUsername("user2");
        roommate2.setEmail("user2@example.com");
        roommate2.setRentAmount(1200.0);
        roommate2.setRentStatus(RentStatus.PAYMENT_PENDING);

        List<Roommate> roommates = Arrays.asList(roommate1, roommate2);

        when(roommateRepo.findAll()).thenReturn(roommates);

        notificationService.setFromMail("noreply@sharespace.com");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(eq("email-template"), any(Context.class))).thenReturn("<html>Email Content</html>");

        MailResponse response = notificationService.sendPendingMail();

        assertEquals("Mail sent successfully to the Remaining Roommates", response.getMessage());
        verify(roommateRepo, times(1)).findAll();
        verify(javaMailSender, times(2)).send(mimeMessage);
    }

    @Test
    void SendPendingMail_NoRoommates() {
        when(roommateRepo.findAll()).thenReturn(List.of());

        RoommateException exception = assertThrows(RoommateException.class, () -> {
            notificationService.sendPendingMail();
        });
        assertEquals("No Roommates Available", exception.getMessage());
        verify(roommateRepo, times(1)).findAll();
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

}