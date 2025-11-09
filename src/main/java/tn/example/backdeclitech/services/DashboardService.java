package tn.example.backdeclitech.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.repositories.ChildRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class DashboardService {

    private final ChildRepository childRepository;

    public Map<String, Long> getAgeGroupsStats() {
        List<Child> children = StreamSupport.stream(childRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        Map<String, Long> ageGroups = new HashMap<>();
        ageGroups.put("08-12", children.stream().filter(c -> c.getAge() >= 8 && c.getAge() <= 12).count());
        ageGroups.put("13-16", children.stream().filter(c -> c.getAge() >= 13 && c.getAge() <= 16).count());

        return ageGroups;
    }
}
