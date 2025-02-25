package sharespace.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityCheck {

    private String roomType;
    private boolean withAC;
    private boolean withFood;
    private int capacity;


}
