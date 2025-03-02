package sharespace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReferralDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int referralId;
    private String username;
    private LocalDate referralDate;
    private String roommateUniqueId;

}
