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
public class SiteExitPresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "presence_id", nullable = false, unique = true)
    private Presence presence;

    @Column(name = "exit_notes", columnDefinition = "TEXT")
    private String exitNotes;

    @Column(name = "id_verified")
    private Boolean idVerified;

    @Column(name = "authorized_pickup")
    private Boolean authorizedPickup;

    @Column(name = "lost_items", columnDefinition = "TEXT")
    private String lostItems;

    @Column(name = "incident_report", columnDefinition = "TEXT")
    private String incidentReport;

    @Column(name = "parent_signature")
    @Builder.Default
    private String parentSignature = null;

    @Column(name = "pickup_signature_obtained")
    @Builder.Default
    private Boolean pickupSignatureObtained = false;

    public void validate() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for exit presence tracking");
        }
        presence.validatePresence();

        if (presence.getSite() == null) {
            throw new IllegalArgumentException("Site is required for exit presence tracking");
        }

        if (Boolean.TRUE.equals(presence.getPresent())) {
            if (authorizedPickup == null) {
                throw new IllegalArgumentException("Authorization status must be specified for pickup");
            }
            if (Boolean.FALSE.equals(authorizedPickup) && !Boolean.TRUE.equals(idVerified)) {
                throw new IllegalArgumentException("ID verification required for non-authorized pickup person");
            }
        }

        if (Boolean.FALSE.equals(presence.getPresent()) &&
            (exitNotes == null || exitNotes.trim().isEmpty())) {
            throw new IllegalArgumentException("Exit notes required when child did not exit as expected");
        }
    }

    public void autoPopulateFields() {
        if (presence == null) {
            throw new IllegalArgumentException("Presence is required for exit presence tracking");
        }
        presence.autoPopulateFields();

        if (authorizedPickup == null && presence.getParent() != null) {
            String parentName = presence.getParentName();
            if (parentName != null) {
                authorizedPickup = true;
            }
        }

        if (Boolean.TRUE.equals(authorizedPickup) && idVerified == null) {
            idVerified = true;
        }
    }

    public boolean isSecurePickup() {
        return Boolean.TRUE.equals(authorizedPickup) && Boolean.TRUE.equals(idVerified);
    }

    public boolean hasIncidents() {
        return incidentReport != null && !incidentReport.trim().isEmpty();
    }

    public boolean isSuccessfulExit() {
        return Boolean.TRUE.equals(presence.getPresent());
    }

    public boolean isProperlyDocumented() {
        return parentSignature != null && Boolean.TRUE.equals(idVerified);
    }

    public String getExitSummary() {
        StringBuilder summary = new StringBuilder();
        if (isSuccessfulExit()) {
            summary.append("Exited");
            if (!isSecurePickup()) {
                summary.append(" - ⚠️ Security concerns");
            }
            if (hasIncidents()) {
                summary.append(" - 📋 Incidents reported");
            }
        } else {
            summary.append("Did not exit");
            if (exitNotes != null) {
                summary.append(" - ").append(exitNotes);
            }
        }
        return summary.toString();
    }

    public String getSecurityLevel() {
        if (Boolean.TRUE.equals(authorizedPickup) && Boolean.TRUE.equals(idVerified)) {
            return "SECURE";
        } else if (Boolean.TRUE.equals(authorizedPickup)) {
            return "AUTHORIZED";
        } else if (Boolean.TRUE.equals(idVerified)) {
            return "VERIFIED";
        } else {
            return "REQUIRES_REVIEW";
        }
    }
}