package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;
import tn.example.backdeclitech.entities.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            AuditEntityType entityType,
            Long entityId
    );


    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByActionTypeOrderByTimestampDesc(AuditActionType actionType);


    List<AuditLog> findByEntityTypeOrderByTimestampDesc(AuditEntityType entityType);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:entityId IS NULL OR a.entityId = :entityId) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) AND " +
            "(:searchTerm IS NULL OR " +
            "LOWER(a.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY a.timestamp DESC")
    List<AuditLog> findWithFilters(
            @Param("actionType") AuditActionType actionType,
            @Param("entityType") AuditEntityType entityType,
            @Param("userId") Long userId,
            @Param("entityId") Long entityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("searchTerm") String searchTerm
    );


    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a " +
            "WHERE (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
            "GROUP BY a.actionType")
    List<Object[]> countByActionType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a " +
            "WHERE (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
            "GROUP BY a.entityType")
    List<Object[]> countByEntityType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT COUNT(a) FROM AuditLog a " +
            "WHERE (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countTotalActions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT COUNT(DISTINCT a.userId) FROM AuditLog a " +
            "WHERE (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countUniqueUsers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT a.userId, a.userName, a.userEmail, COUNT(a) as actionCount " +
            "FROM AuditLog a " +
            "WHERE (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
            "GROUP BY a.userId, a.userName, a.userEmail " +
            "ORDER BY actionCount DESC")
    List<Object[]> findTopUsers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT COUNT(a) FROM AuditLog a " +
            "WHERE a.actionType = 'UPDATE' " +
            "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countModifications(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT COUNT(a) FROM AuditLog a " +
            "WHERE a.actionType IN ('DELETE', 'BULK_DELETE') " +
            "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countDeletions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT COUNT(a) FROM AuditLog a " +
            "WHERE a.actionType = 'CREATE' " +
            "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countCreations(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(a) FROM AuditLog a " +
            "WHERE a.actionType = 'LOGIN' " +
            "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR a.timestamp <= :endDate)")
    Long countLogins(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);


    AuditLog findFirstByEntityTypeAndEntityIdAndActionTypeInOrderByTimestampDesc(
            AuditEntityType entityType,
            Long entityId,
            List<AuditActionType> actionTypes
    );
}