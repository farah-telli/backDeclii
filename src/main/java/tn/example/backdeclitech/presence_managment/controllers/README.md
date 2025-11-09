Of course. Here is the updated `README.md` file with a new, detailed section on how to perform full and partial updates.

-----

# API Documentation: Presence Management System

## Introduction

Welcome to the Presence Management API documentation. This guide provides all the information you need to interact with the endpoints for tracking and managing presences, including module session attendance, site entries, and site exits.

The API is built to be flexible, allowing for complex data retrieval through a dynamic filtering system and providing both full and partial update capabilities.

-----

## 1\. Authentication

All requests to the Presence Management API must be authenticated. You need to include a JSON Web Token (JWT) in the `Authorization` header of every request.

**Header Format:**

```
Authorization: Bearer <Your_JWT_Token>
```

-----

## 2\. Standard API Response

All API responses (except for some `DELETE` requests) are wrapped in a standard `ApiResponse` object. This provides a consistent structure for handling both successful responses and errors.

**Successful Response Structure:**

```json
{
  "success": true,
  "message": "A descriptive message about the result.",
  "data": { ... } // The actual data payload (an object or an array)
}
```

**Error Response Structure:**

```json
{
  "success": false,
  "message": "A descriptive error message.",
  "data": null,
  "error": "A more detailed error description or validation errors."
}
```

-----

## 3\. Dynamic Filtering (`/filter` Endpoints)

The most powerful feature of this API is the ability to dynamically filter results using a `POST` request to any `/filter` endpoint (e.g., `/api/presences/filter`). This allows you to build complex queries with nested logic (AND, OR, NOT) and a variety of operators.

### 3.1. The `GenericFilterRequest` Object

The body of a `/filter` request is a `GenericFilterRequest` object. Here's its structure:

```json
{
  "filters": {
    "fieldName": {
      "operator": "operator_code",
      "values": [ "value1" ],
      "from": "start_value", // For BETWEEN operator
      "to": "end_value"      // For BETWEEN operator
    }
  },
  "and": [ /* Array of GenericFilterRequest objects */ ],
  "or": [ /* Array of GenericFilterRequest objects */ ],
  "not": { /* A single GenericFilterRequest object */ },
  "page": 0,
  "size": 10,
  "sortBy": "fieldName",
  "sortDirection": "ASC" // or "DESC"
}
```

### 3.2. Filter Operators

Here are the operators you can use in the `FilterCriteria` object.

| Operator Name | `operator` Code | Description | Example `values`/`from`/`to` |
| :--- | :--- | :--- | :--- |
| **Equal** | `eq` | Matches an exact value. | `"values": ["PRESENT"]` |
| **Not Equal** | `ne` | Matches values that are not equal. | `"values": ["ABSENT"]` |
| **Like** | `like` | Case-insensitive search for a substring. | `"values": ["John"]` |
| **Greater Than** | `gt` | For numbers and dates. | `"values": [10]` |
| **Less Than** | `lt` | For numbers and dates. | `"values": [20]` |
| **Greater/Equal** | `gte` | For numbers and dates. | `"values": [10]` |
| **Less/Equal** | `lte` | For numbers and dates. | `"values": [20]` |
| **Between** | `between`| Checks if a value is in a range. | `"from": "2025-07-18", "to": "2025-07-20"` |
| **In** | `in` | Matches any value in a list. | `"values": ["PENDING", "ABSENT"]` |
| **Not In**| `nin` | Matches any value not in a list. | `"values": ["COMPLETED"]` |
| **Is Null**| `null` | Checks if a field is null. | (no values needed) |
| **Is Not Null**| `notnull`| Checks if a field is not null. | (no values needed) |

### 3.3. Filter Examples

#### Example 1: Simple Filter

Find all presences where the status is `'PRESENT'`.

**Request:** `POST /api/presences/filter`

**Body:**

```json
{
  "filters": {
    "status": {
      "operator": "eq",
      "values": ["PRESENT"]
    }
  },
  "sortBy": "reservationDate",
  "sortDirection": "DESC"
}
```

#### Example 2: Nested Logic (AND/OR)

Find all presences for the child with ID `123` that are either `'PRESENT'` OR have a `performanceRating` greater than or equal to `4`.

**Request:** `POST /api/module-session-presences/filter`

**Body:**

```json
{
  "and": [
    {
      "filters": {
        "presence.child.id": {
          "operator": "eq",
          "values": [123]
        }
      }
    },
    {
      "or": [
        {
          "filters": {
            "presence.status": {
              "operator": "eq",
              "values": ["PRESENT"]
            }
          }
        },
        {
          "filters": {
            "performanceRating": {
              "operator": "gte",
              "values": [4]
            }
          }
        }
      ]
    }
  ]
}
```

**Note on Nested Fields**: You can filter on nested object properties using dot notation (e.g., `"presence.child.id"`).

-----

## 4\. Updating Records (Full & Partial)

The API provides two methods for updating records: `PUT` for full updates and `PATCH` for partial updates.

### 4.1. Full Update (`PUT`)

A `PUT` request replaces the **entire** existing record with the new data you provide. If you omit any fields in your request body, they will be set to `null` or their default value in the database.

**When to use:** Use `PUT` when you want to update the entire object at once, for instance, in an "Edit" form where all fields are present.

**Example: Full Update of a `ModuleSessionPresence` record**

**Request:** `PUT /api/module-session-presences/5` (where `5` is the record ID)

**Body:** (You must provide the complete `ModuleSessionPresenceDTO` object)

```json
{
    "id": 5,
    "presence": {
        "id": 10,
        "dateTime": "2025-07-18T10:00:00",
        "reservationDate": "2025-07-18",
        "pupilName": "John Doe",
        "present": true,
        "parentName": "Jane Doe",
        "lastUpdated": "2025-07-18T10:05:00",
        "childId": 101,
        "organizingTeamId": 201,
        "parentId": 301,
        "sessionId": 401,
        "siteId": 501,
        "status": "PRESENT"
    },
    "participationNotes": "Excellent participation today.",
    "performanceRating": 5,
    "behavioralNotes": "Very respectful.",
    "activitiesCompleted": true,
    "absenceReason": null
}
```

### 4.2. Partial Update (`PATCH`)

A `PATCH` request updates **only the fields you provide** in the request body. All other fields in the existing record will remain unchanged.

**When to use:** Use `PATCH` for quick, targeted updates, such as changing a single status, updating a boolean flag, or adding a note.

**Example 1: Change the status of a `Presence` record**

**Request:** `PATCH /api/presences/10`

**Body:**

```json
{
  "status": "ABSENT"
}
```

*Result: Only the `status` field of presence record 10 will be changed to `ABSENT`.*

-----

**Example 2: Update notes and rating for a `ModuleSessionPresence` record**

**Request:** `PATCH /api/module-session-presences/5`

**Body:**

```json
{
    "performanceRating": 4,
    "behavioralNotes": "Had a minor issue with another child, but it was resolved quickly."
}
```

*Result: Only the `performanceRating` and `behavioralNotes` are updated. All other fields, including the nested `presence` object, remain the same.*

-----

## 5\. API Endpoints

### 5.1. Presence Controller (`/api/presences`)

Manages generic `Presence` records.

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/presences/filter` | **Dynamically filter for presences.** (See examples above) | `GenericFilterRequest` |
| `GET` | `/api/presences` | Get a paginated list of all presences. | - |
| `GET` | `/api/presences/{id}` | Get a single presence by its ID. | - |
| `POST` | `/api/presences` | Create a new presence record. | `PresenceDTO` |
| `PUT` | `/api/presences/{id}` | **Full update** of a presence record. | `PresenceDTO` |
| `PATCH`| `/api/presences/{id}` | **Partial update** of a presence record. | `Map<String, Object>` e.g., `{"status": "PRESENT"}`|
| `DELETE`| `/api/presences/{id}` | Delete a presence record. | - |

**`PresenceDTO` Object Structure:**

```json
{
  "id": 1,
  "dateTime": "2025-07-18T10:00:00",
  "reservationDate": "2025-07-18",
  "pupilName": "John Doe",
  "present": true,
  "parentName": "Jane Doe",
  "lastUpdated": "2025-07-18T10:05:00",
  "childId": 101,
  "organizingTeamId": 201,
  "parentId": 301,
  "sessionId": 401,
  "siteId": 501,
  "status": "PRESENT"
}
```

-----

### 5.2. Module Session Presence Controller (`/api/module-session-presences`)

Manages presences specifically for module sessions. The DTO includes extra fields like `performanceRating` and `behavioralNotes`.

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/module-session-presences/filter` | **Dynamically filter** for module session presences. | `GenericFilterRequest` |
| `PUT` | `/api/module-session-presences/{id}` | **Full update** of a module session presence. | `ModuleSessionPresenceDTO` |
| `PATCH`| `/api/module-session-presences/{id}`| **Partial update** of a module session presence. | `Map<String, Object>` |
| ... | *(Other endpoints follow the same pattern as `PresenceController`)* | ... | ... |

**`ModuleSessionPresenceDTO` Object Structure:**

```json
{
    "id": 1,
    "presence": { /* PresenceDTO object */ },
    "participationNotes": "Very engaged in the activity.",
    "performanceRating": 5,
    "behavioralNotes": "Worked well with others.",
    "activitiesCompleted": true,
    "absenceReason": null
}
```

-----

### 5.3. Site Entry Presence Controller (`/api/site-entry-presences`)

Manages site entry records. The DTO includes entry-specific fields.

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/site-entry-presences/filter` | **Dynamically filter** for site entries. | `GenericFilterRequest` |
| `PUT` | `/api/site-entry-presences/{id}` | **Full update** of a site entry. | `SiteEntryPresenceDTO` |
| `PATCH`| `/api/site-entry-presences/{id}`| **Partial update** of a site entry. | `Map<String, Object>` |
| ... | *(Other endpoints follow the same pattern as `PresenceController`)* | ... | ... |

**`SiteEntryPresenceDTO` Object Structure:**

```json
{
    "id": 1,
    "presence": { /* PresenceDTO object */ },
    "supplementaryInfo": "Arrived with grandmother.",
    "disciplineReport": null
}
```

-----

### 5.4. Site Exit Presence Controller (`/api/site-exit-presences`)

Manages site exit records. The DTO includes exit-specific fields.

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/site-exit-presences/filter` | **Dynamically filter** for site exits. | `GenericFilterRequest` |
| `PUT` | `/api/site-exit-presences/{id}` | **Full update** of a site exit. | `SiteExitPresenceDTO` |
| `PATCH`| `/api/site-exit-presences/{id}`| **Partial update** of a site exit. | `Map<String, Object>` |
| ... | *(Other endpoints follow the same pattern as `PresenceController`)* | ... | ... |

**`SiteExitPresenceDTO` Object Structure:**

```json
{
    "id": 1,
    "presence": { /* PresenceDTO object */ },
    "exitNotes": "Picked up by father.",
    "authorizedPickup": true,
    "lostItems": "None",
    "idVerified": true,
    "incidentReport": null,
    "pickupSignatureObtained": true
}
```