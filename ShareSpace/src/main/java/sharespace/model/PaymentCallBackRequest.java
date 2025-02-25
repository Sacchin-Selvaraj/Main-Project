package sharespace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCallBackRequest {

        private String paymentId;
        private String orderId;
        private  double amount;
        private String email;

}
