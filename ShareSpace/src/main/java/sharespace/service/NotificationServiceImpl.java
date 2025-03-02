package sharespace.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sharespace.constants.RoomConstants;
import sharespace.exception.NotificationException;
import sharespace.exception.RoommateException;
import sharespace.model.*;
import sharespace.repository.RoomRepository;
import sharespace.repository.RoommateRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final RoommateRepository roommateRepo;
    private final RoomRepository roomRepo;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    @Getter
    @Setter
    private String fromMail;

    public NotificationServiceImpl(JavaMailSender javaMailSender, RoommateRepository roommateRepo, RoomRepository roomRepo, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.roommateRepo = roommateRepo;
        this.roomRepo = roomRepo;
        this.templateEngine = templateEngine;
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public MailResponse sendMailToRoommateAutomatically() {
        log.info("Starting automatic mail sending process for roommates");
        try {
            List<Roommate> roommates = roommateRepo.findAll();
            if (roommates.isEmpty()) {
                log.warn("No roommates found in the Db");
                throw new RoommateException("No Roommates details present");
            }

            Map<String, Roommate> roommatesMap = roommates.stream()
                    .collect(Collectors.toMap(Roommate::getRoommateUniqueId, Function.identity()));

            List<Roommate> updatedRoommates = new ArrayList<>();
            for (Roommate roommate : roommates) {
                roommate.setRentStatus(RentStatus.PAYMENT_PENDING);
                roommate.setRentAmount(calculateRentAmount(roommate, roommatesMap));
                updatedRoommates.add(roommate);
            }

            roommateRepo.saveAll(updatedRoommates);
            log.info("Updated rent status and amount for {} roommates", updatedRoommates.size());

            updatedRoommates.forEach(this::sendMailToRoommate);
            log.info("Mails sent successfully to all roommates");
        } catch (RoommateException e) {
            log.error("Error in sending Mail to Roommate Automatically: ", e);
        }
        return new MailResponse("Mail sent successfully", true);
    }

    private double calculateRentAmount(Roommate roommate, Map<String, Roommate> roommatesMap) {
        log.info("Calculating rent amount for roommate: {}", roommate.getUsername());
        String roomNumber = roommate.getRoomNumber();
        Room room = roomRepo.findByRoomNumber(roomNumber);
        if (room == null) {
            log.error("Room not found for room number: {}", roomNumber);
            throw new RoommateException("Room not found for room number: " + roomNumber);
        }
        double roomRent = room.getPrice();

        List<ReferralDetails> referralDetailsList = roommate.getReferralDetailsList();
        if (referralDetailsList.isEmpty()) {
            log.info("No referral details found for roommate: {}", roommate.getUsername());
            return roomRent;
        }

        int count = (int) referralDetailsList.stream()
                .filter(referral -> roommatesMap.containsKey(referral.getRoommateUniqueId()))
                .count();

        referralDetailsList.removeIf(referral -> !roommatesMap.containsKey(referral.getRoommateUniqueId()));

        log.debug("Final rent calculated for roommate: {}", roommate.getUsername());
        return roomRent - (roomRent * (RoomConstants.REFERRAL_PERCENTAGE * count));
    }

    @Override
    public MailResponse sendMailToRoommate() {
        log.info("Starting manual mail sending process for roommates");
        List<Roommate> roommates = roommateRepo.findAll();
        if (roommates.isEmpty()) {
            log.error("No roommates found in the system");
            throw new RoommateException("No Roommates details present");
        }
        for (Roommate roommate : roommates) {
            sendMailToRoommate(roommate);
        }
        MailResponse mailResponse = new MailResponse();
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
            log.info("Mail sent successfully to: {}", toMail);

        } catch (MessagingException e) {
            log.error("Failed to send mail to: {}", toMail, e);
            throw new NotificationException("Failed to send email");
        }

    }

    @Override
    public MailResponse sendPendingMail() {
        log.info("Starting pending mail process for roommates");
        List<Roommate> roommateList = roommateRepo.findAll();
        if (roommateList.isEmpty()) {
            log.error("No roommates found in the system");
            throw new RoommateException("No Roommates Available");
        }
        boolean flag = true;
        for (Roommate roommate : roommateList) {
            RentStatus roommateRentStatus = roommate.getRentStatus();
            if (RentStatus.PAYMENT_PENDING == roommateRentStatus) {
                sendMailToRoommate(roommate);
                flag = false;
            }
        }
        if (flag) {
            MailResponse mailResponse = new MailResponse();
            mailResponse.setMessage("There are no Payment Pending from Roommates");
            mailResponse.setStatus(Boolean.FALSE);
            log.info("No pending payments found for roommates");
            return mailResponse;
        } else {
            MailResponse mailResponse = new MailResponse();
            mailResponse.setMessage("Mail sent successfully to the Remaining Roommates");
            mailResponse.setStatus(Boolean.TRUE);
            log.info("Mails sent successfully to roommates with pending payments");
            return mailResponse;
        }
    }

    public void sendMailToRoommate(Roommate roommate) {
        log.info("Sending mail to roommate: {}", roommate.getUsername());
        String subject = "Payment Remainder from ShareSpace";
        String toMail = roommate.getEmail();
        Map<String, Object> model = new HashMap<>();
        model.put("username", roommate.getUsername());
        model.put("rentAmount", roommate.getRentAmount());
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        model.put("currentMonth", currentMonth);
        model.put("dueDate", currentDate.plusDays(5));
        sendMail(toMail, subject, model);
    }


}
