package sharespace.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacateResponseDTO {

        private int vacateRequestId;
        private LocalDate checkOutDate;
        private String vacateReason;
        private String roommateName;
        private String roomNumber;
        private LocalDateTime createdAt;
        private Boolean isRead;

}
