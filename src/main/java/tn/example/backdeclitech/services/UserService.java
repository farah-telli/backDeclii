package tn.example.backdeclitech.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService; // ✅ AJOUT

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService // ✅ AJOUT
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User createUser(UserRequest request) {
        if (request.getRole() == Role.PARENT) {
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                throw new IllegalArgumentException("Phone number is required for Parent");
            }
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Phone already exists");
            }
        } else {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required for " + request.getRole());
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        User user = new User();
        user.setUsername(request.getFirstName());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setActive(true);
        user.setRegistrationDate(LocalDateTime.now());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() == Role.PARENT) {
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
        } else {
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
        }

        User savedUser = userRepository.save(user);

        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createAuditLog(
                        AuditActionType.CREATE,
                        AuditEntityType.USER,
                        savedUser.getId(),
                        savedUser.getFirstName() + " " + savedUser.getLastName(),
                        currentUser,
                        null,
                        savedUser,
                        "Création d'un nouvel utilisateur (" + savedUser.getRole() + ")",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }

        return savedUser;
    }

    public List<UserRequest> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserRequest(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.getIsVerified(),
                        user.isActive(),
                        user.getRegistrationDate() != null ? user.getRegistrationDate().toString() : null,
                        user.getAccountExpirationDate() != null ? user.getAccountExpirationDate().toString() : null,
                        user.getCoBuildSpace() != null ? user.getCoBuildSpace().getName() : null
                ))
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Transactional
    public User updateUser(Long id, User userData) {
        User existingUser = getUserById(id);

        User oldUser = cloneUser(existingUser);

        existingUser.setFirstName(userData.getFirstName());
        existingUser.setLastName(userData.getLastName());
        existingUser.setEmail(userData.getEmail());
        existingUser.setPhone(userData.getPhone());
        existingUser.setRole(userData.getRole());
        existingUser.setActive(userData.isActive());
        existingUser.setCoBuildSpace(userData.getCoBuildSpace());

        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);

        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createAuditLog(
                        AuditActionType.UPDATE,
                        AuditEntityType.USER,
                        savedUser.getId(),
                        savedUser.getFirstName() + " " + savedUser.getLastName(),
                        currentUser,
                        oldUser,
                        savedUser,
                        "Modification d'un utilisateur",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }

        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable avec l'ID : " + id);
        }

        User user = getUserById(id);
        String userName = user.getFirstName() + " " + user.getLastName();

        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createAuditLog(
                        AuditActionType.DELETE,
                        AuditEntityType.USER,
                        id,
                        userName,
                        currentUser,
                        user,
                        null,
                        "Suppression d'un utilisateur (" + user.getRole() + ")",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }

        userRepository.deleteById(id);
    }

    public List<InstructorDTO> getAllInstructorDTOs() {
        List<User> instructors = userRepository.findByRole(Role.INSTRUCTOR);
        return instructors.stream().map(user -> {
            InstructorDTO dto = new InstructorDTO();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());

            List<String> moduleNames = user.getModule().stream()
                    .map(module -> module.getTitle() + ", " + module.getJour())
                    .collect(Collectors.toList());

            dto.setModules(moduleNames);
            return dto;
        }).collect(Collectors.toList());
    }

    // --- Statistiques Parents ---
    public Map<String, Long> getParentStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("activeParents", userRepository.countActiveParents());
        stats.put("inactiveParents", userRepository.countInactiveParents());
        stats.put("newParentsLastMonth",
                userRepository.countParentsRegisteredSince(LocalDateTime.now().minusMonths(1))
        );
        return stats;
    }

    public List<ParentChildStatsDTO> getParentChildStats() {
        List<Map<String, Object>> results = userRepository.countChildrenPerParent();
        return results.stream()
                .map(map -> new ParentChildStatsDTO(
                        (Long) map.get("parentId"),
                        (Long) map.get("childCount")
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void bulkActivate(List<Long> userIds) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setActive(true);
                userRepository.save(user);
            }
        }


        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createBulkAuditLog(
                        AuditActionType.BULK_ACTIVATE,
                        AuditEntityType.USER,
                        userIds.size(),
                        currentUser,
                        "Activation groupée de " + userIds.size() + " utilisateur(s)",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }
    }

    @Transactional
    public void bulkDeactivate(List<Long> userIds) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setActive(false);
                userRepository.save(user);
            }
        }

        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createBulkAuditLog(
                        AuditActionType.BULK_DEACTIVATE,
                        AuditEntityType.USER,
                        userIds.size(),
                        currentUser,
                        "Désactivation groupée de " + userIds.size() + " utilisateur(s)",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }
    }

    @Transactional
    public void bulkDelete(List<Long> userIds) {
        // ✅ CRÉER LE LOG D'AUDIT GROUPÉ AVANT LA SUPPRESSION
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createBulkAuditLog(
                        AuditActionType.BULK_DELETE,
                        AuditEntityType.USER,
                        userIds.size(),
                        currentUser,
                        "Suppression groupée de " + userIds.size() + " utilisateur(s)",
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }

        for (Long userId : userIds) {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
            }
        }
    }

    @Transactional
    public void bulkChangeRole(List<Long> userIds, Role newRole) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setRole(newRole);
                userRepository.save(user);
            }
        }

        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                auditLogService.createBulkAuditLog(
                        AuditActionType.BULK_ROLE_CHANGE,
                        AuditEntityType.USER,
                        userIds.size(),
                        currentUser,
                        "Changement de rôle groupé pour " + userIds.size() + " utilisateur(s) vers " + newRole,
                        getCurrentRequest()
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du log d'audit: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }

            if (authentication != null && authentication.getName() != null) {
                return userRepository.findByEmail(authentication.getName()).orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur actuel: " + e.getMessage());
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la requête: " + e.getMessage());
        }
        return null;
    }

    private User cloneUser(User user) {
        User clone = new User();
        clone.setId(user.getId());
        clone.setFirstName(user.getFirstName());
        clone.setLastName(user.getLastName());
        clone.setEmail(user.getEmail());
        clone.setPhone(user.getPhone());
        clone.setRole(user.getRole());
        clone.setActive(user.isActive());
        clone.setUsername(user.getUsername());
        return clone;
    }
}