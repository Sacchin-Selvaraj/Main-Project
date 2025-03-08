package sharespace.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long id;
    private Double amount;
    private String paymentStatus;
    private LocalDate paymentDate;
    private String transactionId;
    private String paymentMethod;
    private String username;
    private String roomNumber;

}
