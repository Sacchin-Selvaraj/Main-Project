package sharespace.service;

import sharespace.model.AvailabilityCheck;
import sharespace.model.Room;
import sharespace.model.Roommate;
import sharespace.payload.RoommateDTO;

import java.util.List;

public interface RoomService {

    List<Room> getAllRoomDetails();

    Room getRoomById(int roomId);

    List<Room> checkAvailability(AvailabilityCheck available);

    RoommateDTO bookRoom(int roomId, Roommate roommate);

    String addRooms(Room room);

    Room editRoom(int roomId,Room room);

    String deleteRoom(int roomId);
}
