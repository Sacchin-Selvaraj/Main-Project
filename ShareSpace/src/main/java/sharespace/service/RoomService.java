package sharespace.service;

import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.RoomDTO;
import sharespace.payload.RoommateDTO;

import java.util.List;

public interface RoomService {

    List<RoomDTO> getAllRoomDetails();

    RoomDTO getRoomById(int roomId);

    List<RoomDTO> checkAvailability(AvailabilityCheck available);

    RoommateDTO bookRoom(int roomId, Roommate roommate);

    String addRooms(Room room);

    RoomDTO editRoom(int roomId,Room room);

    String deleteRoom(int roomId);
}
