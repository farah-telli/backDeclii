package tn.example.backdeclitech.presence_managment.utils;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import tn.example.backdeclitech.presence_managment.dto.PresenceFilterRequest;
import tn.example.backdeclitech.presence_managment.entities.Presence;

import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
public class PresenceSpecifications {

    private final PresenceFilterRequest filter;

    public Specification<Presence> build() {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            addEqualPredicate(root, criteriaBuilder, predicate, "child.id", filter.getChildId());
            addEqualPredicate(root, criteriaBuilder, predicate, "parent.id", filter.getParentId());
            addEqualPredicate(root, criteriaBuilder, predicate, "site.spaceId", filter.getSiteId());
            addEqualPredicate(root, criteriaBuilder, predicate, "session.id", filter.getSessionId());

            addEqualPredicate(root, criteriaBuilder, predicate, "present", filter.getPresent());
            addLikePredicate(root, criteriaBuilder, predicate, "pupilName", filter.getPupilName());
            addLikePredicate(root, criteriaBuilder, predicate, "parentName", filter.getParentName());

            if (filter.getDate() != null) {
                addEqualPredicate(root, criteriaBuilder, predicate, "reservationDate", filter.getDate());
            }

            if (filter.getFilters() != null) {
                for (Map.Entry<String, Object> entry : filter.getFilters().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String && !((String) value).isEmpty()) {
                        addLikePredicate(root, criteriaBuilder, predicate, entry.getKey(), value);
                    } else if (value != null) {
                        addEqualPredicate(root, criteriaBuilder, predicate, entry.getKey(), value);
                    }
                }
            }

            return predicate;
        };
    }

    private void addEqualPredicate(Root<Presence> root, CriteriaBuilder cb, Predicate predicate, String fieldPath,
            Object value) {
        if (value != null) {
            predicate.getExpressions().add(cb.equal(getPath(root, fieldPath), value));
        }
    }

    private void addLikePredicate(Root<Presence> root, CriteriaBuilder cb, Predicate predicate, String fieldPath,
            Object value) {
        if (value != null && !value.toString().trim().isEmpty()) {
            predicate.getExpressions()
                    .add(cb.like(cb.lower(getPath(root, fieldPath)), "%" + value.toString().toLowerCase() + "%"));
        }
    }

    private <V> Path<V> getPath(Root<Presence> root, String path) {
        String[] parts = path.split("\\.");
        Path<V> currentPath = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            currentPath = currentPath.get(parts[i]);
        }
        return currentPath;
    }
}