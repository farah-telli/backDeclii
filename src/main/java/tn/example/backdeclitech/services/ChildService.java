package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.DTO.ChildResponse;
import tn.example.backdeclitech.DTO.UpdateChildRequest;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.events.child_related_events.ChildUpdatedEvent;
import tn.example.backdeclitech.repositories.ChildRepository;
import tn.example.backdeclitech.repositories.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChildService implements IChildService {

    @Autowired
    private UserRepository userRepository;

    private final ChildRepository childRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<ChildResponse> getChildDTOsByParent(User parent) {
        List<Child> children = childRepository.findByParent(parent);
        return children.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ChildResponse> getAllChildren() {
        List<Child> children = (List<Child>) childRepository.findAll();
        return children.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private ChildResponse convertToDTO(Child child) {
        ChildResponse dto = new ChildResponse();
        dto.setId(child.getId());
        dto.setFirstName(child.getFirstName());
        dto.setLastName(child.getLastName());
        dto.setSexe(child.getSexe());
        dto.setAge(child.getAge());
        dto.setParentId(child.getParent() != null ? child.getParent().getId() : null);

        dto.setCobuildSpace(child.getCobuildSpace());
        dto.setTitre(child.getTitre() != null ? child.getTitre().name().toLowerCase() : "explorateur");
        dto.setClasse(child.getClasse());
        dto.setCodeClasse(child.getCodeClasse());
        dto.setPseudonyme(child.getPseudonyme());
        dto.setMotDePasse(child.getMotDePasse());
        dto.setProjets(child.getProjets() != null ? child.getProjets() : List.of());
        dto.setDateDerniereSeance(child.getDateDerniereSeance() != null ?
                child.getDateDerniereSeance().toString() : null);
        dto.setInformations(child.getInformations());

        return dto;
    }

    public Child updateChild(Long childId, UpdateChildRequest request) throws Exception {
        Optional<Child> optionalChild = childRepository.findById(childId);
        if (optionalChild.isEmpty()) {
            throw new Exception("Enfant introuvable avec id : " + childId);
        }

        Child child = optionalChild.get();

        if (request.getCobuildSpace() != null) {
            child.setCobuildSpace(request.getCobuildSpace());
        }
        if (request.getTitre() != null) {
            child.setTitre(Child.Titre.valueOf(request.getTitre().toUpperCase()));
        }
        if (request.getClasse() != null) {
            child.setClasse(request.getClasse());
        }
        if (request.getCodeClasse() != null) {
            child.setCodeClasse(request.getCodeClasse());
        }
        if (request.getPseudonyme() != null) {
            child.setPseudonyme(request.getPseudonyme());
        }
        if (request.getMotDePasse() != null) {
            child.setMotDePasse(request.getMotDePasse());
        }
        if (request.getProjets() != null) {
            child.setProjets(request.getProjets());
        }
        if (request.getDateDerniereSeance() != null && !request.getDateDerniereSeance().isEmpty()) {
            child.setDateDerniereSeance(LocalDate.parse(request.getDateDerniereSeance()));
        }
        if (request.getInformations() != null) {
            child.setInformations(request.getInformations());
        }

        Child savedChild = childRepository.save(child);
        eventPublisher.publishEvent(new ChildUpdatedEvent(this, savedChild));

        return savedChild;
    }

    public List<User> getAllParents() {
        return userRepository.findByRole(Role.PARENT);
    }

    public User addParent(User parent) {
        parent.setRole(Role.PARENT);
        return userRepository.save(parent);
    }

    public Child addChildToParent(Long parentId, Child child) throws Exception {
        Optional<User> optionalParent = userRepository.findById(parentId);
        if (optionalParent.isEmpty()) {
            throw new Exception("Parent introuvable avec id : " + parentId);
        }
        User parent = optionalParent.get();
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent.");
        }

        if (child.getCodeClasse() == null || child.getCodeClasse().isEmpty()) {
            child.setCodeClasse(generateClassCode());
        }
        if (child.getPseudonyme() == null || child.getPseudonyme().isEmpty()) {
            child.setPseudonyme((child.getFirstName() + child.getLastName()).toLowerCase());
        }
        if (child.getMotDePasse() == null || child.getMotDePasse().isEmpty()) {
            child.setMotDePasse(generatePassword());
        }
        if (child.getClasse() == null || child.getClasse().isEmpty()) {
            child.setClasse("Declitech-RI");
        }
        if (child.getTitre() == null) {
            child.setTitre(Child.Titre.EXPLORATEUR);
        }

        child.setParent(parent);
        return childRepository.save(child);
    }

    public List<Child> getChildrenByParent(Long parentId) throws Exception {
        Optional<User> optionalParent = userRepository.findById(parentId);
        if (optionalParent.isEmpty()) {
            throw new Exception("Parent introuvable");
        }
        return childRepository.findByParent(optionalParent.get());
    }

    private String generateClassCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }
}