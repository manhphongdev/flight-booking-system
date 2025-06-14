package dao.interfaces;

import java.util.List;
import model.Seat;

public interface ISeatDAO extends IBaseDAO<Seat> {
    List<Seat> findByFlightId(Long flightId);
    List<Seat> findBySeatClass(String seatClass);
    List<Seat> findByStatus(String status);
    boolean updateStatus(Long seatId, String status);
    public int countUnBookedSeat(long flightId, String seatClass);
    public boolean checkFlightAvailability(long filghtId, String seatClass, int number);
} 