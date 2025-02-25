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
public class VacateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vacateRequestId;
    private LocalDate checkOutDate;
    private String vacateReason;
    private Boolean isRead=false;

    @OneToOne
    @JoinColumn(name = "roommate_id")
    private Roommate roommate;

}
