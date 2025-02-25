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
    private int roomId;
    private int floorNumber;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private int currentCapacity;
    private boolean isAcAvailable;
    private double price;

    @OneToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},orphanRemoval = true)
    private List<Roommate> roommateList =new ArrayList<>();

    public Room(int floorNumber, String roomNumber, String roomType, int capacity, int currentCapacity, boolean isAcAvailable, double price, List<Roommate> roommateList) {
        this.floorNumber = floorNumber;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.currentCapacity = currentCapacity;
        this.isAcAvailable = isAcAvailable;
        this.price = price;
        this.roommateList = roommateList;
    }

    public List<Roommate> getRoommateList() {
        return roommateList;
    }

    public void setRoommateList(List<Roommate> roommateList) {
        this.roommateList = roommateList;
    }
}
