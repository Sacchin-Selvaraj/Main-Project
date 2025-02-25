package sharespace.service;

import sharespace.model.MailResponse;

public interface NotificationService {

    MailResponse sendMailToRoommate();

    MailResponse sendPendingMail();
}
