package sharespace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;
    private Integer floorNumber;
    private String roomNumber;
    private String roomType;
    private Integer capacity;
    private Integer currentCapacity;
    private Boolean isAcAvailable;
    private Double price;
    private Double perDayPrice;

    @OneToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},orphanRemoval = true)
    private List<Roommate> roommateList =new ArrayList<>();

    public Room(Integer floorNumber, String roomNumber, String roomType, Integer capacity, Integer currentCapacity, Boolean isAcAvailable, Double price, Double perDayPrice, List<Roommate> roommateList) {
        this.floorNumber = floorNumber;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.currentCapacity = currentCapacity;
        this.isAcAvailable = isAcAvailable;
        this.price = price;
        this.perDayPrice = perDayPrice;
        this.roommateList = roommateList;
    }

    public List<Roommate> getRoommateList() {
        return roommateList;
    }

    public void setRoommateList(List<Roommate> roommateList) {
        this.roommateList = roommateList;
    }
}
