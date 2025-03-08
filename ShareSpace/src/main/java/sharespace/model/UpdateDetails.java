package sharespace.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDetails {

    @Size(min = 6,message = "Username and Password should have min 6 characters")
    private String username;
    private String password;
    private String email;
    private Boolean withFood;
    private LocalDate checkOutDate;
}
