package tn.example.backdeclitech.services;

import tn.example.backdeclitech.DTO.CreateModuleRequest;
import tn.example.backdeclitech.DTO.InstructorModulesResponse;
import tn.example.backdeclitech.DTO.ModuleResponse;
import tn.example.backdeclitech.DTO.UpdateModuleRequest;

import java.util.List;

public interface ModuleService {

    ModuleResponse createModule(CreateModuleRequest request, String imagePath);

    ModuleResponse updateModule(Long id, UpdateModuleRequest request);

    ModuleResponse getModuleById(Long id);

    List<ModuleResponse> getAllModules();

    List<ModuleResponse> getActiveModules();

    List<ModuleResponse> searchModulesByTitle(String title);

    void deleteModule(Long id);

    void activateModule(Long id);
    void deactivateModule(Long id);

    ModuleResponse updateModuleImage(Long id, String imagePath);
    ModuleResponse updateModuleWithImage(Long id, UpdateModuleRequest request, String imagePath);
    List<InstructorModulesResponse> getModulesByInstructors();
    InstructorModulesResponse getModulesByInstructorId(Long instructorId);
}
