package tn.example.backdeclitech.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.CoBuildSpace;

@Repository
public interface CoBuildSpaceRepository extends CrudRepository<CoBuildSpace,Long> {
}
