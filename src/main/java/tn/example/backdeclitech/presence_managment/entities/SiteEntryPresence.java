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
public class SiteEntryPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "presence_id", nullable = false, unique = true)
    private Presence presence;

    @Column(name = "supplementary_info", columnDefinition = "TEXT")
    private String supplementaryInfo;

    @Column(name = "discipline_report", columnDefinition = "TEXT")
    private String disciplineReport;

    public void validate() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for entry presence tracking");
        }
        presence.validatePresence();

        if (presence.getSite() == null) {
            throw new IllegalArgumentException("Site is required for entry presence tracking");
        }

        if (Boolean.FALSE.equals(presence.getPresent()) &&
            (disciplineReport == null || disciplineReport.trim().isEmpty())) {
            throw new IllegalArgumentException("Reason for entry denial must be provided in discipline report");
        }
    }

    public void autoPopulateFields() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for entry presence tracking");
        }
        presence.autoPopulateFields();
    }

    public boolean hasDisciplineIssues() {
        return disciplineReport != null && !disciplineReport.trim().isEmpty();
    }

    public boolean isSuccessfulEntry() {
        return Boolean.TRUE.equals(presence.getPresent());
    }

    public String getEntrySummary() {
        StringBuilder summary = new StringBuilder();
        if (isSuccessfulEntry()) {
            summary.append("Entry approved");
            if (hasDisciplineIssues()) {
                summary.append(" - Discipline issues");
            }
        } else {
            summary.append("Entry denied");
            if (hasDisciplineIssues()) {
                summary.append(" - ").append(disciplineReport);
            }
        }
        return summary.toString();
    }
}