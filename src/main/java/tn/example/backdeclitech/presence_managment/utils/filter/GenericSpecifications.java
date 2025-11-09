package tn.example.backdeclitech.presence_managment.utils.filter;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

 /**
 * GenericSpecifications is a utility class for building dynamic JPA {@link Specification} objects
 * based on a flexible, generic filter structure. It enables advanced filtering of entities using
 * AND, OR, NOT, and various comparison operators, supporting nested and complex filter logic.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * Specification<User> spec = GenericSpecifications.forEntity(User.class, filterRequest);
 * List<User> users = userRepository.findAll(spec);
 * }
 * </pre>
 * <p>
 * <b>Features:</b>
 * <ul>
 *   <li>Supports field-based filtering with operators: EQUAL, NOT_EQUAL, LIKE, GREATER_THAN, LESS_THAN, etc.</li>
 *   <li>Allows combining filters with AND, OR, and NOT logic, including nested conditions.</li>
 *   <li>Supports filtering on nested properties using dot notation (e.g., "address.city").</li>
 *   <li>Type-safe conversion for filter values, including enums and date/time types.</li>
 *   <li>Optional restriction to a set of allowed fields for security.</li>
 *   <li>Throws {@link InvalidFilterException} for invalid filters or conversion errors.</li>
 * </ul>
 * <p>
 * <b>How to Use:</b>
 * <ol>
 *   <li>Create a {@link GenericFilterRequest} describing your filter logic.</li>
 *   <li>Call {@code GenericSpecifications.forEntity(EntityClass.class, filterRequest)} to get a Specification.</li>
 *   <li>Use the Specification with your Spring Data JPA repository.</li>
 * </ol>
 * <p>
 * <b>Restricting Allowed Fields:</b>
 * <pre>
 * {@code
 * Set<String> allowedFields = Set.of("username", "email", "status");
 * Specification<User> spec = GenericSpecifications.forEntity(User.class, filterRequest, allowedFields);
 * }
 * </pre>
 * <p>
 * <b>Supported Operators:</b>
 * <ul>
 *   <li>EQUAL, NOT_EQUAL, LIKE, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL</li>
 *   <li>BETWEEN, IN, NOT_IN, IS_NULL, IS_NOT_NULL</li>
 * </ul>
 * <p>
 * <b>Notes:</b>
 * <ul>
 *   <li>Field paths must match entity property names (use dot notation for nested fields).</li>
 *   <li>Filter values are automatically converted to the target property type.</li>
 *   <li>For LIKE, only String fields are supported.</li>
 *   <li>For BETWEEN, both 'from' and 'to' values must be provided.</li>
 * </ul>
 *
 * @param <T> The entity type for which the Specification is built.
 * @see org.springframework.data.jpa.domain.Specification
 * @see GenericFilterRequest
 * @see FilterCriteria
 * @see InvalidFilterException
 */
@Slf4j
public class GenericSpecifications<T> {

    private final GenericFilterRequest filter;
    private final Class<T> entityClass;
    private final Set<String> allowedFields;

    private GenericSpecifications(GenericFilterRequest filter, Class<T> entityClass, Set<String> allowedFields) {
        this.filter = Objects.requireNonNull(filter, "Filter cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.allowedFields = allowedFields != null ? allowedFields : Collections.emptySet();
    }

    public static <T> Specification<T> forEntity(Class<T> entityType, GenericFilterRequest filter) {
        return forEntity(entityType, filter, null);
    }

    public static <T> Specification<T> forEntity(Class<T> entityType, GenericFilterRequest filter, 
                                                Set<String> allowedFields) {
        if (filter == null || filter.isEmpty()) {
            return Specification.where(null);
        }
        
        filter.validate();
        return new GenericSpecifications<>(filter, entityType, allowedFields).build();
    }

    private Specification<T> build() {
        return (root, query, cb) -> {
            try {
                Predicate predicate = buildPredicateRecursive(root, cb, filter);
                return predicate != null ? predicate : cb.conjunction();
            } catch (Exception e) {
                log.error("Error building specification for entity {}: {}", 
                         entityClass.getSimpleName(), e.getMessage());
                throw new InvalidFilterException("Failed to build filter specification", e);
            }
        };
    }

    private Predicate buildPredicateRecursive(Root<T> root, CriteriaBuilder cb, GenericFilterRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        // Handle field filters
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            request.getFilters().entrySet().stream()
                .filter(entry -> isFieldAllowed(entry.getKey()))
                .forEach(entry -> {
                    try {
                        Predicate predicate = buildPredicateForCriteria(root, cb, entry.getKey(), entry.getValue());
                        if (predicate != null) {
                            predicates.add(predicate);
                        }
                    } catch (Exception e) {
                        throw new InvalidFilterException(
                            String.format("Error processing filter for field '%s': %s", 
                                        entry.getKey(), e.getMessage()), e);
                    }
                });
        }

        // Handle AND conditions
        if (request.getAnd() != null && !request.getAnd().isEmpty()) {
            request.getAnd().stream()
                .filter(Objects::nonNull)
                .forEach(subRequest -> {
                    Predicate subPredicate = buildPredicateRecursive(root, cb, subRequest);
                    if (subPredicate != null) {
                        predicates.add(subPredicate);
                    }
                });
        }

        // Handle OR conditions
        if (request.getOr() != null && !request.getOr().isEmpty()) {
            List<Predicate> orPredicates = request.getOr().stream()
                .filter(Objects::nonNull)
                .map(subRequest -> buildPredicateRecursive(root, cb, subRequest))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (!orPredicates.isEmpty()) {
                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }
        }

        // Handle NOT condition
        if (request.getNot() != null) {
            Predicate notPredicate = buildPredicateRecursive(root, cb, request.getNot());
            if (notPredicate != null) {
                predicates.add(cb.not(notPredicate));
            }
        }

        return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate buildPredicateForCriteria(Root<T> root, CriteriaBuilder cb, 
                                              String fieldPath, FilterCriteria criteria) {
        if (criteria == null || criteria.getOperator() == null) {
            return null;
        }

        Path<?> path = getPath(root, fieldPath);
        
        switch (criteria.getOperator()) {
            case EQUAL:
                return buildEqualPredicate(cb, path, getFirstValue(criteria.getValues()));
            case NOT_EQUAL:
                return buildNotEqualPredicate(cb, path, getFirstValue(criteria.getValues()));
            case LIKE:
                return buildLikePredicate(cb, path, getFirstValue(criteria.getValues()));
            case GREATER_THAN:
                return buildGreaterThanPredicate(cb, path, getFirstValue(criteria.getValues()));
            case LESS_THAN:
                return buildLessThanPredicate(cb, path, getFirstValue(criteria.getValues()));
            case GREATER_THAN_OR_EQUAL:
                return buildGreaterThanOrEqualPredicate(cb, path, getFirstValue(criteria.getValues()));
            case LESS_THAN_OR_EQUAL:
                return buildLessThanOrEqualPredicate(cb, path, getFirstValue(criteria.getValues()));
            case BETWEEN:
                return buildBetweenPredicate(cb, path, criteria.getFrom(), criteria.getTo());
            case IN:
                return buildInPredicate(cb, path, criteria.getValues());
            case NOT_IN:
                return buildNotInPredicate(cb, path, criteria.getValues());
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            default:
                throw new InvalidFilterException("Unsupported filter operator: " + criteria.getOperator());
        }
    }

    private Object getFirstValue(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private Predicate buildEqualPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return cb.isNull(path);
        }
        return cb.equal(path, convertValue(value, path.getJavaType()));
    }

    private Predicate buildNotEqualPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return cb.isNotNull(path);
        }
        return cb.notEqual(path, convertValue(value, path.getJavaType()));
    }

    private Predicate buildLikePredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            return null;
        }
        
        if (!String.class.isAssignableFrom(path.getJavaType())) {
            throw new InvalidFilterException("LIKE operator can only be used with String fields");
        }
        
        String likeValue = "%" + value.toString().toLowerCase() + "%";
        return cb.like(cb.lower(path.as(String.class)), likeValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildGreaterThanPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return null;
        }
        
        Object convertedValue = convertValue(value, path.getJavaType());
        if (!(convertedValue instanceof Comparable)) {
            throw new InvalidFilterException("GREATER_THAN operator requires comparable values");
        }

        return cb.greaterThan((Path<Comparable>) path, (Comparable) convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildLessThanPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return null;
        }
        
        Object convertedValue = convertValue(value, path.getJavaType());
        if (!(convertedValue instanceof Comparable)) {
            throw new InvalidFilterException("LESS_THAN operator requires comparable values");
        }
        
        return cb.lessThan((Path<Comparable>) path, (Comparable) convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildGreaterThanOrEqualPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return null;
        }
        
        Object convertedValue = convertValue(value, path.getJavaType());
        if (!(convertedValue instanceof Comparable)) {
            throw new InvalidFilterException("GREATER_THAN_OR_EQUAL operator requires comparable values");
        }
        
        return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildLessThanOrEqualPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return null;
        }
        
        Object convertedValue = convertValue(value, path.getJavaType());
        if (!(convertedValue instanceof Comparable)) {
            throw new InvalidFilterException("LESS_THAN_OR_EQUAL operator requires comparable values");
        }
        
        return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildBetweenPredicate(CriteriaBuilder cb, Path<?> path, Object from, Object to) {
        if (from == null || to == null) {
            return null;
        }
        
        Object convertedFrom = convertValue(from, path.getJavaType());
        Object convertedTo = convertValue(to, path.getJavaType());
        
        if (!(convertedFrom instanceof Comparable) || !(convertedTo instanceof Comparable)) {
            throw new InvalidFilterException("BETWEEN operator requires comparable values");
        }
        
        return cb.between((Path<Comparable>) path, (Comparable) convertedFrom, (Comparable) convertedTo);
    }

    private Predicate buildInPredicate(CriteriaBuilder cb, Path<?> path, List<Object> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        
        List<Object> convertedValues = values.stream()
            .filter(Objects::nonNull)
            .map(value -> convertValue(value, path.getJavaType()))
            .collect(Collectors.toList());
        
        return convertedValues.isEmpty() ? null : path.in(convertedValues);
    }

    private Predicate buildNotInPredicate(CriteriaBuilder cb, Path<?> path, List<Object> values) {
        Predicate inPredicate = buildInPredicate(cb, path, values);
        return inPredicate != null ? cb.not(inPredicate) : null;
    }

    private boolean isFieldAllowed(String fieldPath) {
        if (allowedFields.isEmpty()) {
            return true;
        }
        return allowedFields.contains(fieldPath);
    }

    @SuppressWarnings("unchecked")
    private <V> Path<V> getPath(Root<T> root, String fieldPath) {
        try {
            String[] parts = fieldPath.split("\\.");
            Path<?> currentPath = root.get(parts[0]);
            
            for (int i = 1; i < parts.length; i++) {
                currentPath = currentPath.get(parts[i]);
            }
            
            return (Path<V>) currentPath;
        } catch (Exception e) {
            throw new InvalidFilterException("Invalid field path: " + fieldPath, e);
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        try {
            if (targetType == String.class) {
                return value.toString();
            }
            
            String stringValue = value.toString();
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(stringValue);
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(stringValue);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(stringValue);
            }
            if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(stringValue);
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(stringValue);
            }
            if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (targetType.isEnum()) {
                return getEnumValue(targetType, stringValue);
            }
            return value;
            
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new InvalidFilterException(
                String.format("Cannot convert value '%s' to type %s", value, targetType.getSimpleName()), e);
        }
    }

    private Object getEnumValue(Class<?> targetType, String stringValue) {
        if (stringValue == null) {
            return null;
        }
        @SuppressWarnings("rawtypes")
        Class<? extends Enum> enumType = targetType.asSubclass(Enum.class);
        for (Object constant : enumType.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(stringValue)) {
                return constant;
            }
        }
        throw new InvalidFilterException(
            String.format("No enum constant '%s' in %s", stringValue, targetType.getSimpleName()));
    }
}
