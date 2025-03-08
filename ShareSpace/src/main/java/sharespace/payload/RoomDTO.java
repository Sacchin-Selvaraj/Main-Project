package sharespace.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private Integer roomId;
    private Integer floorNumber;
    private String roomNumber;
    private String roomType;
    private Integer capacity;
    private Integer currentCapacity;
    private Boolean isAcAvailable;
    private Double price;
    private Double perDayPrice;

}
