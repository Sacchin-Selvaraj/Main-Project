package sharespace.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grievances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int grievanceId;

    private String grievanceContent;
    private LocalDate grievanceFrom;
    private LocalDate createdAt;
    private Boolean isRead;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "roommateId")
    @JsonBackReference
    private Roommate roommate;

}
