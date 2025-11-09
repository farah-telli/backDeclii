package tn.example.backdeclitech.mappers;

import org.springframework.stereotype.Component;
import tn.example.backdeclitech.DTO.CreateModuleRequest;
import tn.example.backdeclitech.DTO.ModuleResponse;
import tn.example.backdeclitech.DTO.UpdateModuleRequest;
import tn.example.backdeclitech.entities.Module;

import java.util.stream.Collectors;

@Component
public class ModuleMapper {

    public Module toEntity(CreateModuleRequest request) {
        Module module = new Module();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setActif(request.getActif() != null ? request.getActif() : true);
        module.setJour(request.getJour());
        return module;
    }

    public void updateEntity(Module module, UpdateModuleRequest request) {
        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getActif() != null) module.setActif(request.getActif());
        if (request.getJour() != null) module.setJour(request.getJour());
    }

    public ModuleResponse toResponse(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setActif(module.isActif());
        response.setEnrolledCount(module.getEnrolledCount());

        response.setJour(module.getJour());

        if (module.getCoBuildSpace() != null) {
            response.setCoBuildSpaceId(module.getCoBuildSpace().getSpaceId());
            response.setCoBuildSpaceName(module.getCoBuildSpace().getName());
        }

        if (module.getInstructors() != null) {
            var names = module.getInstructors().stream()
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .collect(Collectors.toList());

            response.setInstructeurs(names);
        }

        return response;
    }
}

