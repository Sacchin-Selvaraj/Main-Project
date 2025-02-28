package sharespace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    private String paymentStatus;
    private LocalDate paymentDate;
    private String transactionId;
    private String paymentMethod;
    private String username;
    private String roomNumber;

    public Payment(Double amount, String paymentStatus, LocalDate paymentDate, String transactionId, String paymentMethod, String username, String roomNumber) {
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.username = username;
        this.roomNumber = roomNumber;
    }
}
