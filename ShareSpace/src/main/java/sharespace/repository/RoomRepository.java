package sharespace.repository;

import org.springframework.data.repository.query.Param;
import sharespace.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room,Integer> {

    @Query("SELECT r from Room r where r.roomNumber=?1")
    Room findByRoomNumber(String roomNumber);

    @Query("SELECT COUNT(r) > 0 FROM Room r WHERE LOWER(r.roomNumber) = LOWER(:roomNumber)")
    boolean existsByRoomNumber(@Param("roomNumber") String roomNumber);

    @Query("SELECT r.perDayPrice FROM Room r where roomNumber = :roomNumber ")
    Double findPerDayPrice(@Param("roomNumber") String roomNumber);
}
