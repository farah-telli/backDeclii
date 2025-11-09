package tn.example.backdeclitech.mappers;

import lombok.extern.slf4j.Slf4j;
import tn.example.backdeclitech.DTO.ModuleSessionResponse;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.entities.ModuleSession;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ModuleSessionMapper {

    public static ModuleSessionResponse toDto(ModuleSession session) {
        log.debug("🔍 Mapping session ID: {}", session.getId());

        String jour = session.getDate() != null ? session.getDate().toString() : "N/A";

        String heure = "N/A";
        if (session.getStartTime() != null && session.getEndTime() != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            heure = session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter);
        }

        // ✅ LOGS DE DÉBOGAGE POUR INSTRUCTEURS
        List<String> instructeurs = new ArrayList<>();

        log.debug("   Module: {}", session.getModule() != null ? session.getModule().getTitle() : "NULL");

        if (session.getModule() != null) {
            log.debug("   Instructors list: {}", session.getModule().getInstructors());

            if (session.getModule().getInstructors() != null) {
                log.debug("   Nombre d'instructeurs: {}", session.getModule().getInstructors().size());

                if (!session.getModule().getInstructors().isEmpty()) {
                    instructeurs = session.getModule().getInstructors().stream()
                            .map(instructor -> {
                                String firstName = instructor.getFirstName() != null ? instructor.getFirstName() : "";
                                String lastName = instructor.getLastName() != null ? instructor.getLastName() : "";
                                String fullName = (firstName + " " + lastName).trim();
                                log.debug("      Instructeur mappé: {}", fullName);
                                return fullName;
                            })
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList());
                } else {
                    log.warn("   ⚠️ Liste d'instructeurs vide pour le module {}", session.getModule().getTitle());
                }
            } else {
                log.warn("   ⚠️ Liste d'instructeurs NULL pour le module {}", session.getModule().getTitle());
            }
        } else {
            log.warn("   ⚠️ Module NULL pour la session {}", session.getId());
        }

        if (instructeurs.isEmpty()) {
            log.warn("   ⚠️ Aucun instructeur trouvé, ajout de 'Aucun'");
            instructeurs.add("Aucun");
        }

        log.debug("   ✅ Instructeurs finaux: {}", instructeurs);

        int enrolledCount = 0;
        if (session.getReservations() != null) {
            enrolledCount = session.getReservations().size();
        }

        Long moduleId = session.getModule() != null ? session.getModule().getId() : null;
        Long coBuildSpaceId = session.getCoBuildSpace() != null ? session.getCoBuildSpace().getSpaceId() : null;

        ModuleSessionResponse response = new ModuleSessionResponse();
        response.setId(session.getId());
        response.setJour(jour);
        response.setHeure(heure);
        response.setCoBuildSpaceName(session.getCoBuildSpace() != null ? session.getCoBuildSpace().getName() : "N/A");
        response.setModuleTitle(session.getModule() != null ? session.getModule().getTitle() : "N/A");
        response.setInstructeurs(instructeurs);
        response.setTranche(session.getFormattedTranche());
        response.setIsActive(session.isActive());
        response.setAnnule(session.isAnnule());
        response.setEnrolledCount(enrolledCount);
        response.setCapacity(session.getCapcity());
        response.setModuleId(moduleId);
        response.setCoBuildSpaceId(coBuildSpaceId);

        return response;
    }
}