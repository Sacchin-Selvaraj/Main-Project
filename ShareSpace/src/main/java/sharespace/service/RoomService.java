package sharespace.service;

import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.OwnerRoomDTO;
import sharespace.payload.RoomDTO;
import sharespace.payload.RoommateDTO;

import java.util.List;

public interface RoomService {

    List<OwnerRoomDTO> getAllRoomDetails();

    RoomDTO getRoomById(int roomId);

    List<RoomDTO> checkAvailability(AvailabilityCheck available);

    String bookRoom(int roomId, Roommate roommate);

    String addRooms(Room room);

    RoomDTO editRoom(int roomId,Room room);

    String deleteRoom(int roomId);
}
