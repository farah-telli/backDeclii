package tn.example.backdeclitech.presence_managment.entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.CoBuildSpace;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.entities.User;

import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    indexes = {
        @Index(name = "idx_reservation_date", columnList = "reservationDate"),
        @Index(name = "idx_site_id", columnList = "site_id"),
        @Index(name = "idx_session_id", columnList = "session_id"),
            }
)
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "pupil_name")
    private String pupilName;

    @Column(name = "present", nullable = false)
    private Boolean present;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizing_team_id")
    private User organizingTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ModuleSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private CoBuildSpace site;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PresenceStatus status = PresenceStatus.PENDING;

    @OneToOne(mappedBy = "presence", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ModuleSessionPresence moduleSessionPresence;

    @OneToOne(mappedBy = "presence", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SiteEntryPresence siteEntryPresence;

    @OneToOne(mappedBy = "presence", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SiteExitPresence siteExitPresence;

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
        if (this.reservationDate == null) {
            this.reservationDate = LocalDateTime.now();
        }
    }

    public void validatePresence() {
        if (child == null) {
            throw new IllegalArgumentException("Child is required for presence tracking");
        }
        if (present == null) {
            throw new IllegalArgumentException("Presence status must be specified");
        }
    }

    public void autoPopulateFields() {
        if (child != null) {
            this.pupilName = buildPupilName(child);
            if (parent == null && child.getParent() != null) {
                this.parent = child.getParent();
            }
        }
        if (parent != null && this.parentName == null) {
            this.parentName = buildParentName(parent);
        }
    }

    private String buildPupilName(Child child) {
        if (child == null)
            return null;
        String firstName = child.getFirstName() != null ? child.getFirstName() : "";
        String lastName = child.getLastName() != null ? child.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    private String buildParentName(User parent) {
        if (parent == null)
            return null;
        String firstName = parent.getFirstName() != null ? parent.getFirstName() : "";
        String lastName = parent.getLastName() != null ? parent.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}
