package sharespace.service;

import sharespace.exception.NotificationException;
import sharespace.exception.RoommateException;
import sharespace.model.MailResponse;
import sharespace.model.RentStatus;
import sharespace.model.Roommate;
import sharespace.repository.RoommateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;

    private final RoommateRepository roommateRepo;

    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromMail;

    public NotificationServiceImpl(JavaMailSender javaMailSender, RoommateRepository roommateRepo, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.roommateRepo = roommateRepo;
        this.templateEngine = templateEngine;
    }


    @Override
    @Scheduled(cron = "0 * 1 * *  ?")
    public MailResponse sendMailToRoommate() {

        List<Roommate> roommates = roommateRepo.findAll();
        if (roommates.isEmpty())
            throw new RoommateException("No Roommates details present");
        for (Roommate roommate : roommates) {
            roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
            sendMailToRoommate(roommate);
        }
        MailResponse mailResponse=new MailResponse();
        mailResponse.setMessage("Mail sent successfully");
        mailResponse.setStatus(Boolean.TRUE);
        return mailResponse;
    }

    private void sendMail(String toMail, String subject, Map<String, Object> model) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            context.setVariables(model);
            String htmlContent = templateEngine.process("email-template", context);

            helper.setFrom(fromMail);
            helper.setTo(toMail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email");
        }

    }

    @Override
    public MailResponse sendPendingMail() {
        List<Roommate> roommateList=roommateRepo.findAll();
        if (roommateList.isEmpty())
            throw new RoommateException("No Roommates Available");
        System.out.println("room type list "+roommateList);
        boolean flag=true;
        for (Roommate roommate:roommateList){
            RentStatus roommateRentStatus=roommate.getRentStatus();
            RentStatus rentStatus=RentStatus.PAYMENT_PENDING;
            if (rentStatus==roommateRentStatus){
                sendMailToRoommate(roommate);
                System.out.println("Inside the for loop");
                flag=false;
            }
        }
        if (flag){
            MailResponse mailResponse = new MailResponse();
            mailResponse.setMessage("There are no Pending Payment");
            mailResponse.setStatus(Boolean.TRUE);
            return mailResponse;
        }else {
            MailResponse mailResponse = new MailResponse();
            mailResponse.setMessage("Mail sent successfully to the Remaining Roommates");
            mailResponse.setStatus(Boolean.TRUE);
            return mailResponse;
        }
    }

    public void sendMailToRoommate(Roommate roommate) {
            String subject = "Payment Remainder from ShareSpace";
            String toMail = roommate.getEmail();
            Map<String, Object> model = new HashMap<>();
            model.put("username", roommate.getUsername());
            model.put("rentAmount", roommate.getRentAmount());
            LocalDate currentDate=LocalDate.now();
            Month currentMonth=currentDate.getMonth();
            model.put("currentMonth", currentMonth);
            model.put("dueDate",currentDate.plusDays(5));
            sendMail(toMail,subject,model);
    }


}
