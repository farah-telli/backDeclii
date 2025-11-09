package tn.example.backdeclitech.presence_managment.utils.filter;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a filter criterion used for querying data with various operators
 * and values.
 * <p>
 * This class encapsulates the operator and the associated values required to
 * perform filtering
 * operations such as equality, range, inclusion, and null checks. It provides
 * validation logic
 * to ensure that the required values are present for each operator type.
 * </p>
 *
 * <p>
 * Supported operators include:
 * <ul>
 * <li>EQUAL, NOT_EQUAL, LIKE, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL,
 * LESS_THAN_OR_EQUAL: require at least one value in {@code values}.</li>
 * <li>BETWEEN: requires both {@code from} and {@code to} values.</li>
 * <li>IN, NOT_IN: require a non-empty list in {@code values}.</li>
 * <li>IS_NULL, IS_NOT_NULL: do not require any value.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * FilterCriteria criteria = new FilterCriteria(FilterOperator.EQUAL);
 * criteria.setValues(Collections.singletonList("example"));
 * criteria.validate();
 * </pre>
 * </p>
 *
 * @author DecliTech Team
 * @version 1.0
 */
@Data
public class FilterCriteria {
    @NonNull
    private FilterOperator operator;

    private List<Object> values;
    private Object from;
    private Object to;

    @JsonCreator
    public FilterCriteria(@JsonProperty("operator") FilterOperator operator) {
        this.operator = Objects.requireNonNull(operator, "Filter operator cannot be null");
    }

    /**
     * Validates the criteria based on the operator requirements
     */
    public void validate() {
        switch (operator) {
            case EQUAL:
            case NOT_EQUAL:
            case LIKE:
            case GREATER_THAN:
            case LESS_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN_OR_EQUAL:
                if (values == null || values.isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("Operator %s requires at least one value", operator));
                }
                break;
            case BETWEEN:
                if (from == null || to == null) {
                    throw new IllegalArgumentException("BETWEEN operator requires both 'from' and 'to' values");
                }
                break;
            case IN:
            case NOT_IN:
                if (values == null || values.isEmpty()) {
                    throw new IllegalArgumentException(
                            String.format("Operator %s requires a non-empty list of values", operator));
                }
                break;
            case IS_NULL:
            case IS_NOT_NULL:
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    public enum FilterOperator {
        EQUAL("eq"),
        NOT_EQUAL("ne"),
        LIKE("like"),
        GREATER_THAN("gt"),
        LESS_THAN("lt"),
        GREATER_THAN_OR_EQUAL("gte"),
        LESS_THAN_OR_EQUAL("lte"),
        BETWEEN("between"),
        IN("in"),
        NOT_IN("nin"),
        IS_NULL("null"),
        IS_NOT_NULL("notnull");

        private final String code;

        FilterOperator(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonCreator
        public static FilterOperator fromCode(String code) {
            for (FilterOperator op : values()) {
                if (op.code.equals(code) || op.name().equalsIgnoreCase(code)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown filter operator: " + code);
        }
    }
}
