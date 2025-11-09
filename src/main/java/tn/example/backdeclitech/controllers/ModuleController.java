package tn.example.backdeclitech.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.CoBuildSpace;
import tn.example.backdeclitech.entities.Module;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.exception.ResourceNotFoundException;
import tn.example.backdeclitech.repositories.CoBuildSpaceRepository;
import tn.example.backdeclitech.repositories.ModuleRepository;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.ModuleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoBuildSpaceRepository coBuildSpaceRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/createmodule")
    public ResponseEntity<ModuleResponse> createModule(
            @RequestPart("module") CreateModuleRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        String imagePath = null;

        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/modules/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            imagePath = path.toString();
        }

        ModuleResponse response = moduleService.createModule(request, imagePath);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModuleById(@PathVariable Long id) {
        ModuleResponse response = moduleService.getModuleById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/updatemodule/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long id,
            @RequestPart("module") UpdateModuleRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        String imagePath = null;

        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/modules/";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            imagePath = path.toString();
        }

        ModuleResponse response = moduleService.updateModuleWithImage(id, request, imagePath);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/getallmodule")
    public ResponseEntity<List<ModuleResponse>> getAllModules() {
        List<ModuleResponse> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/getactive/active")
    public ResponseEntity<List<ModuleResponse>> getActiveModules() {
        List<ModuleResponse> modules = moduleService.getActiveModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ModuleResponse>> searchModulesByTitle(@RequestParam String title) {
        List<ModuleResponse> modules = moduleService.searchModulesByTitle(title);
        return ResponseEntity.ok(modules);
    }

    @DeleteMapping("/deletemodule/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateModule(@PathVariable Long id) {
        moduleService.activateModule(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateModule(@PathVariable Long id) {
        moduleService.deactivateModule(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModuleResponse> uploadModuleImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {

        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String uploadDir = "uploads/modules/";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) uploadFolder.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path path = Paths.get(uploadDir + fileName);
        Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        ModuleResponse response = moduleService.updateModuleImage(id, path.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments/assign-full")
    public ResponseEntity<?> assignSingleModuleToInstructor(@RequestBody AssignModuleRequest request) {

        Optional<User> userOpt = userRepository.findById(request.getInstructorId());
        Optional<Module> moduleOpt = moduleRepository.findById(request.getModuleId());
        Optional<CoBuildSpace> spaceOpt = coBuildSpaceRepository.findById(request.getCoBuildSpaceId());

        if (userOpt.isEmpty() || moduleOpt.isEmpty() || spaceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Données invalides"));
        }

        User instructor = userOpt.get();
        Module module = moduleOpt.get();
        CoBuildSpace coBuildSpace = spaceOpt.get();

        if (instructor.getModule() == null) {
            instructor.setModule(new ArrayList<>());
        }
        if (module.getInstructors() == null) {
            module.setInstructors(new ArrayList<>());
        }

        if (instructor.getModule().contains(module)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Module déjà assigné à cet instructeur"));
        }

        instructor.setCoBuildSpace(coBuildSpace);

        instructor.getModule().add(module);
        module.getInstructors().add(instructor);

        if (request.getJour() != null) {
            module.setJour(request.getJour());
        }

        userRepository.save(instructor);

        return ResponseEntity.ok(Map.of("message", "Module affecté avec succès"));
    }

    /**
     * Récupérer tous les instructeurs avec leurs modules assignés
     */
    @GetMapping("/instructors/assignments")
    public ResponseEntity<List<InstructorModulesResponse>> getModulesByInstructors() {
        List<InstructorModulesResponse> response = moduleService.getModulesByInstructors();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/instructors/{instructorId}/assignments")
    public ResponseEntity<InstructorModulesResponse> getModulesByInstructorId(
            @PathVariable Long instructorId) {
        InstructorModulesResponse response = moduleService.getModulesByInstructorId(instructorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructors/my-assignments")
    public ResponseEntity<InstructorModulesResponse> getMyModules(
            @AuthenticationPrincipal UserDetails userDetails) {

        User instructor = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        InstructorModulesResponse response = buildInstructorModulesResponse(instructor);
        return ResponseEntity.ok(response);
    }

    private InstructorModulesResponse buildInstructorModulesResponse(User instructor) {
        InstructorModulesResponse response = new InstructorModulesResponse();
        response.setInstructorId(instructor.getId());
        response.setInstructorName(instructor.getFirstName() + " " + instructor.getLastName());
        response.setInstructorEmail(instructor.getEmail());

        List<ModuleAssignmentInfo> moduleInfos = new ArrayList<>();

        if (instructor.getModule() != null) {
            for (Module module : instructor.getModule()) {
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

                moduleInfos.add(info);
            }
        }

        response.setModules(moduleInfos);
        return response;
    }
}