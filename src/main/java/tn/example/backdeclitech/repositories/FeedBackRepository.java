package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.example.backdeclitech.entities.FeedBack;
import tn.example.backdeclitech.entities.Sentiment;

import java.util.List;

public interface FeedBackRepository extends JpaRepository<FeedBack, Integer> {

    boolean existsByReservationId(Long reservationId);

    @Query("SELECT AVG(f.rating) FROM FeedBack f WHERE f.module.id = :moduleId")
    Double getAverageRatingByModuleId(@Param("moduleId") Long moduleId);

    @Query("SELECT f FROM FeedBack f WHERE f.module.id = :moduleId ORDER BY f.submittedAt DESC")
    List<FeedBack> findRecentFeedbacksByModuleId(@Param("moduleId") Long moduleId, org.springframework.data.domain.Pageable pageable);

    long countBySentiment(Sentiment sentiment);

}