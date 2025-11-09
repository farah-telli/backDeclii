package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.example.backdeclitech.entities.Reservation;
import tn.example.backdeclitech.entities.StatusReservation;
import tn.example.backdeclitech.entities.User;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = "SELECT COUNT(*) FROM reservation r " +
            "JOIN module_session ms ON r.session_id = ms.id " +
            "WHERE r.child_id = :childId " +
            "AND YEAR(ms.date) = :year " +
            "AND WEEK(ms.date, 1) = :weekOfYear " +
            "AND r.status = 'RESERVED'",
            nativeQuery = true)
    long countReservationsForChildInWeek(@Param("childId") Long childId,
                                         @Param("weekOfYear") int weekOfYear,
                                         @Param("year") int year);

    @Query(value = "SELECT COUNT(*) FROM reservation r " +
            "JOIN module_session ms ON r.session_id = ms.id " +
            "WHERE r.child_id = :childId " +
            "AND ms.date >= :startDate " +
            "AND ms.date <= :endDate " +
            "AND r.status = 'RESERVED'",
            nativeQuery = true)
    long countReservationsForChildInDateRange(@Param("childId") Long childId,
                                              @Param("startDate") java.sql.Date startDate,
                                              @Param("endDate") java.sql.Date endDate);

    List<Reservation> findByParentId(Long parentId);

    List<Reservation> findByChildId(Long childId);

    List<Reservation> findByModuleIdAndParentId(Long moduleId, Long parentId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
            "WHERE r.child.id = :childId AND r.session.id = :sessionId AND r.status = 'RESERVED'")
    boolean existsByChildIdAndSessionId(@Param("childId") Long childId, @Param("sessionId") Long sessionId);

    List<Reservation> findByStatus(StatusReservation status);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "JOIN r.session s " +
            "WHERE r.child.id = :childId " +
            "AND FUNCTION('DATE', s.date) = FUNCTION('DATE', :sessionDate) " +
            "AND r.status = 'RESERVED'")
    boolean hasReservationOnSameDay(@Param("childId") Long childId,
                                    @Param("sessionDate") java.util.Date sessionDate);

    long countByParentAndStatus(User parent, StatusReservation status);

    long countByStatus(StatusReservation status);

}