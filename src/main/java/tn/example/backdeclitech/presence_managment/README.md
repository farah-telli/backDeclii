# Presence Management Module

## 1. Overview

The Presence Management module is a specialized system designed to track and manage the attendance of children for various activities, including specific module sessions and general site entry/exit.

It operates on an event-driven and highly automated architecture, which decouples it from other parts of the application, such as the `Reservation` system. This ensures that the presence tracking logic is centralized, robust, and easy to maintain.

The core of the system uses a single-table inheritance strategy for the `Presence` entity, allowing different types of presence records (e.g., `ModuleSessionPresence`, `SiteEntryPresence`) to be managed in a unified way while still catering to their specific data needs.

## 2. Core Workflow & Automation

The system is designed to minimize manual intervention by automating the entire lifecycle of a presence record, from creation to final status.

### a. Automatic Creation of Pending Presences

The creation of presence records is not manual; it is triggered automatically when a reservation is successfully made elsewhere in the system.

1.  **Event Trigger**: When a reservation is made, the `ReservationService` publishes a `ReservationCreatedEvent`.
2.  **Event Listener**: The `PresenceListener` is subscribed to this event.
3.  **Presence Record Creation**: Upon catching the event, the listener immediately calls the `PresenceService` to create the necessary presence records (for the module, site entry, and site exit). These new records are automatically given a default status of `PENDING`.

This ensures that for every active reservation, a corresponding presence record exists and is ready to be updated, eliminating the need for manual creation.

### b. Automatic Deletion on Cancellation

Similarly, the system handles the cleanup of presence records when a reservation is canceled.

1.  **Event Trigger**: If a user cancels a reservation, the `ReservationService` publishes a `ReservationCanceledEvent`.
2.  **Event Listener**: The `PresenceListener` also listens for this event.
3.  **Presence Record Deletion**: The listener then instructs the `PresenceService` to find and delete all associated `PENDING` presence records. This keeps the presence table clean and synchronized with the state of the reservations.

### c. Automatic Marking of Absences (Scheduled Job)

To handle cases where a child does not show up for a reserved session (no-shows), the system includes a daily scheduled task.

1.  **Scheduled Task**: A job within `PresenceServiceImpl` is configured to run automatically every day at a set time (e.g., 9 PM).
2.  **Query for Pending Records**: The job queries the database for any presence records for the current day that are still in the `PENDING` state.
3.  **Update Status to Absent**: It then updates the status of all these records to `ABSENT`. This automates the administrative task of marking absences and ensures that every record is accurately finalized by the end of the day.

## 3. Key Features

* **Decoupled Architecture**: The use of a Spring event-driven model (`ApplicationEventPublisher` and `@EventListener`) makes the Presence module entirely independent of the Reservation module. This loose coupling is crucial for maintainability and scalability, as changes in one system do not directly impact the other.

* **Unified Presence Model**: The system effectively uses a single database table to manage different types of presence (module sessions, site entry, site exit). This is achieved through JPA's inheritance strategy, with a discriminator column to differentiate between presence types.

* **Automated Lifecycle Management**: The entire lifecycle of a presence record—creation, deletion, and finalization (as `PRESENT` or `ABSENT`)—is fully automated. This significantly reduces the need for manual administrative work and minimizes the risk of human error.

* **Comprehensive Status Model**: The `PresenceStatus` enum (`PENDING`, `PRESENT`, `ABSENT`) provides a clear and robust state machine for every presence record. This makes it easy to query and understand the status of any given attendance record at any time.

* **Dynamic and Type-Safe Filtering**: The controllers expose endpoints that use a `GenericFilterRequest` object, allowing clients to build complex, nested queries with various operators. This provides maximum flexibility for data retrieval without needing to write new backend code for every new filtering requirement.