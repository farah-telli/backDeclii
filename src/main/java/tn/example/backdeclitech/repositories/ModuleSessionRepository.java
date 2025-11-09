package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.ModuleSession;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ModuleSessionRepository extends CrudRepository<ModuleSession,Long> {
    @Query("SELECT m FROM ModuleSession m WHERE m.trancheAgeMin <= :age AND m.trancheAgeMax >= :age")
    List<ModuleSession> findByAgeRange(@Param("age") int age);

    List<ModuleSession> findByModuleId(Long moduleId);

    List<ModuleSession> findByModuleTitleContainingIgnoreCase(String moduleName);

    @Query("SELECT m FROM ModuleSession m WHERE FUNCTION('DAYNAME', m.date) = :dayName")
    List<ModuleSession> findByDayName(@Param("dayName") String dayName);
    List<ModuleSession> findByCoBuildSpace_SpaceId(Long spaceId);

    @Query("SELECT ms FROM ModuleSession ms WHERE :age BETWEEN ms.trancheAgeMin AND ms.trancheAgeMax")
    List<ModuleSession> findByChildAge(@Param("age") int age);

    @Query("SELECT DISTINCT ms FROM ModuleSession ms " +
            "WHERE (:days IS NULL OR FUNCTION('DAYNAME', ms.date) IN :days) " +
            "AND (:spaceIds IS NULL OR ms.coBuildSpace.spaceId IN :spaceIds) " +
            "AND (:moduleIds IS NULL OR ms.module.id IN :moduleIds)")
    List<ModuleSession> findFilteredSessions(
            @Param("days") List<String> days,
            @Param("spaceIds") List<Long> spaceIds,
            @Param("moduleIds") List<Long> moduleIds);

    @Query("SELECT DISTINCT ms FROM ModuleSession ms " +
            "WHERE (:days IS NULL OR FUNCTION('DAYNAME', ms.date) IN :days) " +
            "AND (:spaceIds IS NULL OR ms.coBuildSpace.spaceId IN :spaceIds) " +
            "AND (:moduleIds IS NULL OR ms.module.id IN :moduleIds) " +
            "AND (:moduleName IS NULL OR ms.module.title LIKE %:moduleName%)")
    List<ModuleSession> findFilteredSessions(
            @Param("days") List<String> days,
            @Param("spaceIds") List<Long> spaceIds,
            @Param("moduleIds") List<Long> moduleIds,
            @Param("moduleName") String moduleName);

    /* @Query("SELECT m FROM ModuleSession m WHERE m.date BETWEEN :startDate AND :endDate AND m.module.id = :moduleId AND m.coBuildSpace.id = :coBuildSpaceId")
     List<ModuleSession> findByDateBetweenAndModuleIdAndCoBuildSpaceId(
             @Param("startDate") Date startDate,
             @Param("endDate") Date endDate,
             @Param("moduleId") Long moduleId,
             @Param("coBuildSpaceId") Long coBuildSpaceId);
 }*/
//    @Query("SELECT m FROM ModuleSession m WHERE m.module.id = :moduleId AND m.coBuildSpace.spaceId = :coBuildSpaceId AND m.date BETWEEN :startDate AND :endDate AND m.isAnnule = false")
//    List<ModuleSession> findActiveSessionsForModuleAndCoBuildSpaceBetweenDates(
//            @Param("moduleId") Long moduleId,
//            @Param("coBuildSpaceId") Long coBuildSpaceId,
//            @Param("startDate") Date startDate,
//            @Param("endDate") Date endDate
//    );
//
//
//
//    @Query("SELECT ms FROM ModuleSession ms WHERE ms.isAnnule = true " +
//            "AND ms.dateAnnulation IS NOT NULL " +
//            "AND ms.dateAnnulation < :date")
//    List<ModuleSession> findSessionsAnnuleesAvant(@Param("date") Date date);



    List<ModuleSession> findByIsActiveTrue();


    @Query("SELECT ms FROM ModuleSession ms " +
            "WHERE ms.module.id = :moduleId " +
            "AND ms.coBuildSpace.spaceId = :coBuildSpaceId " +
            "AND ms.date BETWEEN :startDate AND :endDate " +
            "AND ms.isActive = true " +
            "AND ms.isAnnule = false")
    List<ModuleSession> findActiveSessionsForModuleAndCoBuildSpaceBetweenDates(
            @Param("moduleId") Long moduleId,
            @Param("coBuildSpaceId") Long coBuildSpaceId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("SELECT ms FROM ModuleSession ms " +
            "WHERE ms.isAnnule = true " +
            "AND ms.dateAnnulation IS NOT NULL " +
            "AND ms.dateAnnulation < :date")
    List<ModuleSession> findSessionsAnnuleesAvant(@Param("date") Date date);


    @Query("SELECT DISTINCT ms FROM ModuleSession ms " +
            "LEFT JOIN FETCH ms.module m " +
            "LEFT JOIN FETCH m.instructors " +  // ✅ Important
            "LEFT JOIN FETCH ms.coBuildSpace")
    List<ModuleSession> findAllWithInstructors();
}