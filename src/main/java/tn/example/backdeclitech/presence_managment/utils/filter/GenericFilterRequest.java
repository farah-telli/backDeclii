package tn.example.backdeclitech.presence_managment.utils.filter;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
/**
 * Represents a generic filter request structure for querying data with flexible filtering,
 * logical composition, and pagination options.
 * <p>
 * This class supports:
 * <ul>
 *     <li>Field-based filtering using a map of field names to {@link FilterCriteria}.</li>
 *     <li>Logical composition of filters using AND, OR, and NOT operators, allowing for
 *         nested and complex filter expressions.</li>
 *     <li>Pagination and sorting options, including page number, page size, sort field,
 *         and sort direction.</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * <pre>
 *     GenericFilterRequest request = GenericFilterRequest.builder()
 *         .filters(Map.of("status", new FilterCriteria(...)))
 *         .page(0)
 *         .size(20)
 *         .sortBy("createdDate")
 *         .sortDirection(GenericFilterRequest.SortDirection.DESC)
 *         .build();
 * </pre>
 * </p>
 * 
 * <p>
 * The {@link #validate()} method can be used to ensure the filter request is well-formed,
 * checking for valid field names, criteria, logical operator structure, and pagination values.
 * </p>
 * 
 * <p>
 * The {@link #isEmpty()} method checks if the filter request contains any filtering logic.
 * The {@link #hasPagination()} method checks if both page and size are specified.
 * </p>
 * 
 * @author DecliTech
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericFilterRequest {
    private Map<String, FilterCriteria> filters;
    private List<GenericFilterRequest> and;
    private List<GenericFilterRequest> or;
    private GenericFilterRequest not;

    private Integer page;
    private Integer size;
    private String sortBy;
    @Builder.Default
    private SortDirection sortDirection = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }

    /**
     * Validates the entire filter request structure
     */
    public void validate() {
        validateFilters();
        validateLogicalOperators();
        validatePagination();
    }

    private void validateFilters() {
        if (filters != null) {
            filters.forEach((fieldName, criteria) -> {
                if (fieldName == null || fieldName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Filter field name cannot be null or empty");
                }
                if (criteria == null) {
                    throw new IllegalArgumentException("Filter criteria cannot be null for field: " + fieldName);
                }
                criteria.validate();
            });
        }
    }

    private void validateLogicalOperators() {
        if (and != null) {
            and.forEach(request -> {
                if (request != null) {
                    request.validate();
                }
            });
        }
        if (or != null) {
            or.forEach(request -> {
                if (request != null) {
                    request.validate();
                }
            });
        }
        if (not != null) {
            not.validate();
        }
    }

    private void validatePagination() {
        if (page != null && page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size != null && size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (filters == null || filters.isEmpty()) &&
               (and == null || and.isEmpty()) &&
               (or == null || or.isEmpty()) &&
               not == null;
    }

    @JsonIgnore
    public boolean hasPagination() {
        return page != null && size != null;
    }
}
