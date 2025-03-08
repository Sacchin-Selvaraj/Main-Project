package sharespace.service;

import sharespace.model.MailResponse;

import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    CompletableFuture<MailResponse> sendMailToRoommate();

    MailResponse sendPendingMail();

    MailResponse sendMailToRoommateAutomatically();
}
