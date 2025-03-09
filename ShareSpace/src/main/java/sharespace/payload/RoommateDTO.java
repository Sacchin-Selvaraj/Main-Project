package sharespace.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoommateDTO {

    private int roommateId;
    private String username;
    private String password;
    private String email;
    private String gender;
    private double rentAmount;
    private String rentStatus;
    private boolean withFood;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String referralId;
    private int referCount;
    private String roomNumber;
}
