package tn.example.backdeclitech.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.example.backdeclitech.entities.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}
