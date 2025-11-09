package tn.example.backdeclitech.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.Penalty;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    Optional<Penalty> findByChildId(Long childId);

}
