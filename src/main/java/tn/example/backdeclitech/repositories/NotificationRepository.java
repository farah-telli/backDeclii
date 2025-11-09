package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.Notification;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.userId IS NULL OR n.userId = :userId ORDER BY n.timestamp DESC")
    List<Notification> findByUserIdIsNullOrUserId(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.read = false AND (n.userId IS NULL OR n.userId = :userId) ORDER BY n.timestamp DESC")
    List<Notification> findUnreadByUserId(Long userId);

    void deleteByTimestampBefore(LocalDateTime timestamp);
}