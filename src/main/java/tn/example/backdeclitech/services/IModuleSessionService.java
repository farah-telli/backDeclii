package tn.example.backdeclitech.services;

import tn.example.backdeclitech.entities.ModuleSession;

import java.util.List;
import java.util.Optional;

public interface IModuleSessionService {
    List<ModuleSession> findAll();
    Optional<ModuleSession> findById(Long id);
    ModuleSession save(ModuleSession session);
    ModuleSession update(Long id, ModuleSession updatedSession);
    void delete(Long id);
    List<ModuleSession> getSessionsByModuleIdSimple(Long moduleId);
    List<ModuleSession> getSessionsByDayName(String dayName);
    List<ModuleSession> getSessionsByCoBuildSpaceId(Long coBuildSpaceId);
    List<ModuleSession> annulerSessionsDureeSemaine(String startDate, Long moduleId, Long coBuildSpaceId);

}
