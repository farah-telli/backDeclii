package tn.example.backdeclitech.repositories;

import org.hibernate.annotations.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRole(Role role);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    long countByRole(Role role);
    long countByRoleAndActive(Role role, boolean active);
    long countByActive(boolean active);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role AND (
            LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    Page<User> findByRoleAndSearchTerm(
            @Param("role") Role role,
            @Param("search") String search,
            Pageable pageable
    );

    List<User> findByRoleAndRegistrationDateBetween(Role role, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT u) 
        FROM User u 
        JOIN u.children c
        WHERE u.role = tn.example.backdeclitech.entities.Role.PARENT
    """)
    long countParentsWithChildren();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'PARENT' AND u.active = true")
    long countActiveParents();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'PARENT' AND u.active = false")
    long countInactiveParents();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'PARENT' AND u.registrationDate >= :date")
    long countParentsRegisteredSince(@Param("date") LocalDateTime date);

    @Query("SELECT u.id AS parentId, COUNT(c) AS childCount " +
            "FROM User u LEFT JOIN u.children c " +
            "WHERE u.role = 'PARENT' " +
            "GROUP BY u.id")
    List<Map<String, Object>> countChildrenPerParent();

    long countByRoleAndAccountExpirationDateBefore(Role role, LocalDate date);


    List<User> findByRoleAndAccountExpirationDateBetween(
            Role role,
            LocalDate startDate,
            LocalDate endDate
    );

    List<User> findByRoleAndAccountExpirationDateBeforeAndActive(
            Role role,
            LocalDate date,
            boolean active
    );

    List<User> findByRoleAndAccountExpirationDateIsNull(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.accountExpirationDate < :currentDate")
    List<User> findExpiredParents(
            @Param("role") Role role,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("""
        SELECT u FROM User u 
        WHERE u.role = :role 
        AND u.accountExpirationDate BETWEEN :today AND :futureDate 
        AND u.active = true
    """)
    List<User> findParentsExpiringSoon(
            @Param("role") Role role,
            @Param("today") LocalDate today,
            @Param("futureDate") LocalDate futureDate
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.accountExpirationDate IS NOT NULL")
    long countParentsWithExpirationDate(@Param("role") Role role);


    @Query("""
        SELECT 
            CASE 
                WHEN u.accountExpirationDate IS NULL THEN 'NO_EXPIRATION'
                WHEN u.accountExpirationDate < :today THEN 'EXPIRED'
                WHEN u.accountExpirationDate BETWEEN :today AND :soonDate THEN 'EXPIRING_SOON'
                ELSE 'ACTIVE'
            END as status,
            COUNT(u) as count
        FROM User u 
        WHERE u.role = :role
        GROUP BY 
            CASE 
                WHEN u.accountExpirationDate IS NULL THEN 'NO_EXPIRATION'
                WHEN u.accountExpirationDate < :today THEN 'EXPIRED'
                WHEN u.accountExpirationDate BETWEEN :today AND :soonDate THEN 'EXPIRING_SOON'
                ELSE 'ACTIVE'
            END
    """)
    List<Map<String, Object>> countParentsByExpirationStatus(
            @Param("role") Role role,
            @Param("today") LocalDate today,
            @Param("soonDate") LocalDate soonDate
    );

    @Query("SELECT u FROM User u WHERE u.role = tn.example.backdeclitech.entities.Role.PARENT AND MONTH(u.registrationDate) = :month AND YEAR(u.registrationDate) = :year")
    List<User> findParentsByMonthAndYear(@Param("month") int month, @Param("year") int year);

    Page<User> findByRegistrationDateBetweenAndRole(
            LocalDateTime start,
            LocalDateTime end,
            Role role,
            Pageable pageable
    );

    @Query("SELECT DISTINCT u FROM User u WHERE u.role = 'PARENT' OR u.role = 'ROLE_PARENT'")
    List<User> findAllParents();

    @Query("SELECT DISTINCT r.parent FROM Reservation r WHERE r.status = 'CONFIRMED' OR r.status = 'PENDING'")
    List<User> findParentsWithActiveReservations();
}