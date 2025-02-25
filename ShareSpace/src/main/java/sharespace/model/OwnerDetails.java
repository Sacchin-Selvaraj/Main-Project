package sharespace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OwnerDetails")
public class OwnerDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ownerId;
    private String ownerName;
    private String password;

    public OwnerDetails(String ownerName, String password) {
        this.ownerName = ownerName;
        this.password = password;
    }
}
