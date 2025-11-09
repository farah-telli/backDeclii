package tn.example.backdeclitech.presence_managment.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSessionPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "presence_id", nullable = false, unique = true)
    private Presence presence;

    @Column(name = "participation_notes", columnDefinition = "TEXT")
    private String participationNotes;

    @Column(name = "performance_rating")
    private Integer performanceRating;

    @Column(name = "behavioral_notes", columnDefinition = "TEXT")
    private String behavioralNotes;

    @Column(name = "activities_completed")
    private Boolean activitiesCompleted;

    @Column(name = "absence_reason")
    private String absenceReason;

    public void validate() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for session attendance tracking");
        }
        presence.validatePresence();

        if (presence.getSession() == null) {
            throw new IllegalArgumentException("Module session is required for session attendance tracking");
        }

        if (performanceRating != null && (performanceRating < 1 || performanceRating > 5)) {
            throw new IllegalArgumentException("Performance rating must be between 1 and 5");
        }

        if (Boolean.FALSE.equals(presence.getPresent()) && (absenceReason == null || absenceReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Absence reason is required when child is not present");
        }
    }

    public void autoPopulateFields() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for session attendance tracking");
        }
        presence.autoPopulateFields();

        if (presence.getSite() == null && presence.getSession() != null && presence.getSession().getCoBuildSpace() != null) {
            presence.setSite(presence.getSession().getCoBuildSpace());
        }

        if (activitiesCompleted == null && presence.getPresent() != null) {
            activitiesCompleted = presence.getPresent();
        }
    }

    public boolean isHighPerformance() {
        return performanceRating != null && performanceRating >= 4;
    }

    public boolean hasBehavioralIssues() {
        return behavioralNotes != null && !behavioralNotes.trim().isEmpty();
    }

    public String getSessionSummary() {
        StringBuilder summary = new StringBuilder();
        if (Boolean.TRUE.equals(presence.getPresent())) {
            summary.append("Present");
            if (Boolean.TRUE.equals(activitiesCompleted)) {
                summary.append(" - Activities completed");
            }
            if (performanceRating != null) {
                summary.append(" - Performance: ").append(performanceRating).append("/5");
            }
        } else {
            summary.append("Absent");
            if (absenceReason != null) {
                summary.append(" - Reason: ").append(absenceReason);
            }
        }
        return summary.toString();
    }
}