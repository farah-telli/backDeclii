package tn.example.backdeclitech.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import tn.example.backdeclitech.entities.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByTitleContainingIgnoreCase(String title);

    List<Module> findByActifTrue();

    @Query("SELECT m FROM Module m WHERE m.coBuildSpace.spaceId = :id")
    List<Module> findByCoBuildSpaceSpaceId(@Param("id") Long spaceId);



    Optional<Module> findByTitle(String title);

    boolean existsByTitle(String title);
}
