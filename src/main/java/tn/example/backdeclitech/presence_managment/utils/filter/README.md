# Dynamic Filter Framework

## Overview

This document provides a comprehensive guide to the **Dynamic Filter Framework**, a powerful and flexible filtering system designed for Spring Data JPA repositories. It allows for the construction of complex, nested queries directly from API parameters, providing a seamless bridge between your API and your data layer. This framework is built to be both easy to use for simple queries and powerful enough for intricate, real-world filtering scenarios.

The framework is built around a set of core components that work together to provide a robust and secure filtering experience. These components include:

  * **`FilterCriteria`**: Defines individual filter conditions with a wide range of operators.
  * **`GenericFilterRequest`**: A container for one or more `FilterCriteria` objects, allowing for logical grouping (AND/OR/NOT) and pagination.
  * **`GenericSpecifications`**: The engine that dynamically builds JPA `Specification` objects from a `GenericFilterRequest`.
  * **`InvalidFilterException`**: A custom exception for handling malformed or invalid filter requests.

This guide will walk you through each of these components, explain how they work together, and provide detailed examples to get you started.

-----

## Core Components

### `FilterCriteria`

The `FilterCriteria` class is the most granular component of the filtering framework. It represents a single filter condition and is composed of an **operator** and the **values** to filter by.

#### **Operators**

The framework supports 12 distinct filter operators, each with a specific purpose:

| Operator | Code | Description |
| :--- | :--- | :--- |
| **EQUAL** | `eq` | Checks for equality. |
| **NOT\_EQUAL** | `ne` | Checks for inequality. |
| **LIKE** | `like` | Performs a case-insensitive substring search (e.g., `%value%`). |
| **GREATER\_THAN** | `gt` | Checks if a value is greater than the specified value. |
| **LESS\_THAN** | `lt` | Checks if a value is less than the specified value. |
| **GREATER\_THAN\_OR\_EQUAL** | `gte` | Checks if a value is greater than or equal to the specified value. |
| **LESS\_THAN\_OR\_EQUAL** | `lte` | Checks if a value is less than or equal to the specified value. |
| **BETWEEN** | `between` | Checks if a value is within a given range (inclusive). |
| **IN** | `in` | Checks if a value is present in a list of values. |
| **NOT\_IN** | `nin` | Checks if a value is not present in a list of values. |
| **IS\_NULL** | `null` | Checks if a field is `NULL`. |
| **IS\_NOT\_NULL** | `notnull` | Checks if a field is not `NULL`. |

#### **Values**

The `FilterCriteria` class has three fields for specifying filter values:

  * `values`: A `List<Object>` used for operators that accept one or more values (e.g., `EQUAL`, `IN`, `LIKE`).
  * `from`: An `Object` representing the start of a range for the `BETWEEN` operator.
  * `to`: An `Object` representing the end of a range for the `BETWEEN` operator.

The class includes a `validate()` method to ensure that the required values are provided for each operator. For example, the `BETWEEN` operator requires both `from` and `to` values to be present.

### `GenericFilterRequest`

The `GenericFilterRequest` class is a container that holds the filtering logic. It supports:

  * **Field-based filters**: A `Map<String, FilterCriteria>` where the key is the field name and the value is the `FilterCriteria` to apply.
  * **Logical grouping**: The ability to combine multiple filter requests using `and`, `or`, and `not` operators, allowing for the creation of complex, nested queries.
  * **Pagination and sorting**: Controls for specifying the page number, page size, sort field, and sort direction.

### `GenericSpecifications`

This is the core of the framework's backend. The `GenericSpecifications` class is responsible for:

  * **Dynamically building JPA `Specification` objects**: It takes a `GenericFilterRequest` and the entity class as input and constructs a `Specification` that can be used with a Spring Data JPA repository.
  * **Field security**: It can be configured with a `Set<String>` of allowed field names to prevent unauthorized fields from being used in filters.
  * **Automatic type conversion**: It automatically converts filter values to the correct type for the corresponding entity field, including support for `String`, `Integer`, `Long`, `Double`, `Float`, `Boolean`, `LocalDate`, `LocalDateTime`, and `Enum` types.
  * **Comprehensive error handling**: It throws an `InvalidFilterException` if it encounters any issues during the specification-building process, such as invalid field paths, type conversion failures, or disallowed field access.

### `InvalidFilterException`

This is a custom `RuntimeException` that is thrown when the filtering framework encounters an invalid or malformed filter request. This can happen for several reasons, including:

  * **Missing required values for an operator**
  * **Invalid field paths**
  * **Type conversion failures**
  * **Accessing a field that is not in the allowlist**

-----

## How it Works

The filtering process begins when a `GenericFilterRequest` is sent to your API. This request is then passed to the `GenericSpecifications.forEntity()` method, which kicks off the following steps:

1.  **Validation**: The `GenericFilterRequest` is validated to ensure that it is well-formed. This includes checking for valid operators, required values, and correct pagination parameters.

2.  **Specification Building**: The `GenericSpecifications` class recursively traverses the `GenericFilterRequest` and builds a JPA `Predicate` for each filter condition. These predicates are then combined using the specified logical operators (`AND`, `OR`, `NOT`) to create a single, composite `Predicate`.

3.  **Field Security**: If an `allowedFields` set is provided, the framework checks each field in the filter request against this set. If an unauthorized field is found, an `InvalidFilterException` is thrown.

4.  **Type Conversion**: For each filter, the framework inspects the entity's field to determine the target type. It then attempts to convert the provided filter value to this type. If the conversion fails, an `InvalidFilterException` is thrown.

5.  **Execution**: The final `Specification` is passed to the `findAll()` method of your Spring Data JPA repository, which executes the query against the database and returns the results.

-----

## Usage Examples

### Basic Presence Filtering

This example demonstrates a simple filter to find all "PRESENT" presences that occurred between 8:00 AM and 12:00 PM on July 18, 2025.

```java
GenericFilterRequest filter = GenericFilterRequest.builder()
    .filters(Map.of(
        "status", new FilterCriteria(EQUAL)
            .setValues(List.of("PRESENT")),
        "entryTime", new FilterCriteria(BETWEEN)
            .setFrom(LocalDateTime.parse("2025-07-18T08:00"))
            .setTo(LocalDateTime.parse("2025-07-18T12:00"))
    ))
    .page(0)
    .size(10)
    .sortBy("exitTime")
    .sortDirection(DESC)
    .build();

List<Presence> results = presenceRepository.findAll(
    GenericSpecifications.forEntity(Presence.class, filter)
);
```

### Complex Child Attendance Query

This example showcases the power of nested logical operators to create a more complex query.

```java
GenericFilterRequest filter = GenericFilterRequest.builder()
    .and(List.of(
        // Filter 1: Child age >= 6 AND grade in [A, B]
        GenericFilterRequest.builder()
            .filters(Map.of(
                "child.age", new FilterCriteria(GTE).setValues(List.of(6)),
                "child.grade", new FilterCriteria(IN).setValues(List.of("A", "B"))
            )).build(),

        // Filter 2: Duration >= 30 OR sessionType = FULL_DAY
        GenericFilterRequest.builder()
            .or(List.of(
                GenericFilterRequest.builder()
                    .filters(Map.of("duration",
                        new FilterCriteria(GTE).setValues(List.of(30)))
                    ).build(),
                GenericFilterRequest.builder()
                    .filters(Map.of("sessionType",
                        new FilterCriteria(EQUAL).setValues(List.of("FULL_DAY")))
                    ).build()
            )).build()
    ))
    .build();
```

-----

## Security Best Practices

### 1\. Field Allowlisting

**Always** use a field allowlist to control which fields can be filtered. This is a critical security measure to prevent exposing sensitive data or allowing queries on unindexed fields that could impact performance.

```java
Set<String> safeFields = Set.of("entryTime", "exitTime", "status");
Specification<Presence> spec = GenericSpecifications.forEntity(
    Presence.class,
    filter,
    safeFields
);
```

### 2\. Input Validation

Before executing a query, validate the filter request to catch any malformed or invalid input.

```java
try {
    filter.validate();
    repository.findAll(spec);
} catch (InvalidFilterException ex) {
    // Handle malformed filters
}
```

### 3\. Pagination Limits

Enforce a maximum page size to prevent clients from requesting an excessive number of records, which could lead to performance degradation or denial-of-service vulnerabilities.

```java
if (filter.getSize() > 100) {
    throw new IllegalArgumentException("Max page size is 100");
}
```

-----

## API Integration Example

Here is an example of how to integrate the filter framework into a Spring Boot `@RestController`.

```java
@GetMapping("/presences")
public ResponseEntity<List<Presence>> getPresences(
    @Valid GenericFilterRequest filterRequest,
    @RequestParam(required = false) Set<String> allowedFields) {

    // It's recommended to define the allowed fields on the server-side
    // for security reasons, rather than accepting them from the client.
    Set<String> serverSideAllowedFields = Set.of("entryTime", "exitTime", "status", "child.age", "child.grade", "duration", "sessionType");

    if (allowedFields != null && !serverSideAllowedFields.containsAll(allowedFields)) {
        // Or handle this as a bad request
        throw new SecurityException("Attempt to filter on disallowed fields.");
    }

    Specification<Presence> spec = GenericSpecifications.forEntity(
        Presence.class,
        filterRequest,
        serverSideAllowedFields
    );

    if (filterRequest.hasPagination()) {
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(),
            filterRequest.getSize(),
            Sort.by(filterRequest.getSortDirection(),
                    filterRequest.getSortBy())
        );
        return ResponseEntity.ok(
            presenceRepository.findAll(spec, pageable).getContent()
        );
    }

    return ResponseEntity.ok(presenceRepository.findAll(spec));
}
```

-----

## Error Handling

The framework is designed to provide clear and informative error messages when it encounters invalid input. Here are some of the most common error cases and the exception messages they produce:

| Error Case | Exception Message Example |
| :--- | :--- |
| **Invalid Operator** | `"Unsupported filter operator: STARTS_WITH"` |
| **Missing Required Value** | `"BETWEEN operator requires both 'from' and 'to' values"` |
| **Invalid Field Path** | `"Invalid field path: user.profile.age"` |
| **Type Conversion Failure** | `"Cannot convert 'abc' to type Integer"` |