package sharespace.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrievancesDTO {

    private int grievanceId;
    private String grievanceContent;
    private LocalDate createdAt;
    private Boolean isRead;
    private String roommateName;
    private String roomNumber;

}
