package tn.example.backdeclitech.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.User;

import java.util.List;

@Repository
public interface ChildRepository extends CrudRepository<Child,Long> {
    List<Child> findByParent(User parent);
    long count();

}
