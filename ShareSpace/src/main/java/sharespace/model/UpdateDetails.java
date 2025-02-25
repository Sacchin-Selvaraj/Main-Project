package sharespace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDetails {

    private String username;
    private String password;
    private String email;
    private Boolean withFood;
    private LocalDate checkOutDate;
}
