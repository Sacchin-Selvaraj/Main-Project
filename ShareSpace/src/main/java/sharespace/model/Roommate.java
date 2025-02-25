package sharespace.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Roommate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int roommateId;
    @NotNull
    @Size(min = 6)
    private String username;
    @NotNull
    @Size(min = 6)
    private String password;
    @Email
    @NotBlank
    private String email;
    private String gender;
    private double rentAmount;
    private RentStatus rentStatus;
    private Boolean withFood;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String referralId;
    private int referralCount;
    private String roomNumber;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Payment> paymentList;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ReferralDetails> referralDetailsList;


    @OneToMany(mappedBy = "roommate",fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<Grievances> grievances=new ArrayList<>();

}

