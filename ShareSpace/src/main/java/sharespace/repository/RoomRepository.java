package sharespace.repository;

import sharespace.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room,Integer> {

    @Query("SELECT r from Room r where r.roomNumber=?1")
    Room findByRoomNumber(String roomNumber);
}
