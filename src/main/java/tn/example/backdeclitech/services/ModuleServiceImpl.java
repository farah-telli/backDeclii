package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.CoBuildSpace;
import tn.example.backdeclitech.entities.Module;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.exception.ResourceNotFoundException;
import tn.example.backdeclitech.exception.ValidationException;
import tn.example.backdeclitech.mappers.ModuleMapper;
import tn.example.backdeclitech.repositories.CoBuildSpaceRepository;
import tn.example.backdeclitech.repositories.ModuleRepository;
import tn.example.backdeclitech.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoBuildSpaceRepository coBuildSpaceRepository;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ModuleResponse createModule(CreateModuleRequest request, String imagePath) {
        Module module = moduleMapper.toEntity(request);

        if (request.getCoBuildSpaceId() != null) {
            CoBuildSpace space = coBuildSpaceRepository.findById(request.getCoBuildSpaceId())
                    .orElseThrow(() -> new RuntimeException("CoBuildSpace introuvable"));
            module.setCoBuildSpace(space);
        }

        module.setImageUrl(imagePath);
        moduleRepository.save(module);

        return moduleMapper.toResponse(module);
    }

    @Override
    public ModuleResponse updateModule(Long id, UpdateModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec l'ID: " + id));

        if (request.getTitle() != null && !request.getTitle().equals(module.getTitle())) {
            if (moduleRepository.existsByTitle(request.getTitle())) {
                throw new ValidationException("Un module avec ce titre existe déjà");
            }
        }

        moduleMapper.updateEntity(module, request);

        if (request.getCoBuildSpaceId() != null) {
            CoBuildSpace coBuildSpace = coBuildSpaceRepository.findById(request.getCoBuildSpaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("CoBuildSpace non trouvé avec l'ID: " + request.getCoBuildSpaceId()));
            module.setCoBuildSpace(coBuildSpace);
        }

        return moduleMapper.toResponse(moduleRepository.save(module));
    }

    @Override
    public ModuleResponse updateModuleWithImage(Long id, UpdateModuleRequest request, String imagePath) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec l'ID: " + id));

        moduleMapper.updateEntity(module, request);

        if (request.getCoBuildSpaceId() != null) {
            CoBuildSpace coBuildSpace = coBuildSpaceRepository.findById(request.getCoBuildSpaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("CoBuildSpace non trouvé"));
            module.setCoBuildSpace(coBuildSpace);
        }

        if (imagePath != null) {
            module.setImageUrl(imagePath);
        }

        return moduleMapper.toResponse(moduleRepository.save(module));
    }


    @Override
    public ModuleResponse getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module introuvable avec id: " + id));
        return moduleMapper.toResponse(module);
    }

    @Override
    public List<ModuleResponse> getAllModules() {
        return moduleRepository.findAll()
                .stream()
                .map(moduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModuleResponse> getActiveModules() {
        return moduleRepository.findByActifTrue()
                .stream()
                .map(moduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModuleResponse> searchModulesByTitle(String title) {
        return moduleRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(moduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module non trouvé avec l'ID: " + id);
        }
        moduleRepository.deleteById(id);
    }

    @Override
    public void activateModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec l'ID: " + id));
        module.setActif(true);
        moduleRepository.save(module);
    }

    @Override
    public void deactivateModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec l'ID: " + id));
        module.setActif(false);
        moduleRepository.save(module);
    }

    @Override
    public ModuleResponse updateModuleImage(Long id, String imagePath) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec l'ID: " + id));

        module.setImageUrl(imagePath);
        return moduleMapper.toResponse(moduleRepository.save(module));
    }

    @Override
    public List<InstructorModulesResponse> getModulesByInstructors() {
        // Récupérer tous les instructeurs qui ont des modules assignés
        List<User> instructors = userRepository.findAll().stream()
                .filter(user -> user.getModule() != null && !user.getModule().isEmpty())
                .collect(Collectors.toList());

        return instructors.stream()
                .map(this::mapInstructorToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InstructorModulesResponse getModulesByInstructorId(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructeur non trouvé avec l'ID: " + instructorId));

        return mapInstructorToResponse(instructor);
    }

    private InstructorModulesResponse mapInstructorToResponse(User instructor) {
        InstructorModulesResponse response = new InstructorModulesResponse();
        response.setInstructorId(instructor.getId());
        response.setInstructorName(instructor.getFirstName() + " " + instructor.getLastName());
        response.setInstructorEmail(instructor.getEmail());

        List<ModuleAssignmentInfo> moduleInfos =
                instructor.getModule().stream()
                        .map(module -> {
                            ModuleAssignmentInfo info =
                                    new ModuleAssignmentInfo();
                            info.setModuleId(module.getId());
                            info.setModuleTitle(module.getTitle());
                            info.setModuleDescription(module.getDescription());
                            info.setJour(module.getJour());
                            info.setActif(module.isActif());
                            info.setAnnule(module.isAnnulation());

                            if (module.getCoBuildSpace() != null) {
                                info.setCoBuildSpaceId(module.getCoBuildSpace().getSpaceId());
                                info.setCoBuildSpaceName(module.getCoBuildSpace().getName());
                            }

                            return info;
                        })
                        .collect(Collectors.toList());

        response.setModules(moduleInfos);
        return response;
    }
}
