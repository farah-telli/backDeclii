package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.entities.Child;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.ChildRepository;
import tn.example.backdeclitech.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminParentService {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminParentResponse createParentWithChildren(AdminCreateParentRequest request, String adminUsername) throws Exception {
        log.info("🔵 Création parent: {}", request.getPhone());

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new Exception("Un parent avec ce numéro de téléphone existe déjà");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Un utilisateur avec cet email existe déjà");
        }

        User parent = new User();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setPhone(request.getPhone());
        parent.setEmail(request.getEmail());
        parent.setUsername(request.getPhone());
        parent.setRole(Role.PARENT);
        parent.setActive(true);
        parent.setIsVerified(true);
        parent.setRegistrationDate(LocalDateTime.now());

        LocalDate expirationDate = (request.getExpirationDate() != null)
                ? request.getExpirationDate()
                : LocalDate.now().plusMonths(1);
        parent.setAccountExpirationDate(expirationDate);
        log.info("📅 Date d'expiration abonnement: {}", expirationDate);

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword() : generateTemporaryPassword();
        parent.setPassword(passwordEncoder.encode(rawPassword));

        User savedParent = userRepository.save(parent);
        log.info("✅ Parent sauvegardé avec ID: {}", savedParent.getId());

        List<Child> children = null;
        if (request.getChildren() != null && !request.getChildren().isEmpty()) {
            children = request.getChildren().stream()
                    .map(childReq -> createChildEntity(childReq, savedParent))
                    .collect(Collectors.toList());
            children = StreamSupport.stream(childRepository.saveAll(children).spliterator(), false)
                    .collect(Collectors.toList());
            log.info("✅ {} enfant(s) créé(s)", children.size());
        }

        return convertToAdminParentResponse(savedParent, children, adminUsername);
    }

    public Page<AdminParentResponse> getAllParentsForAdmin(Pageable pageable, String search) {
        Page<User> parents;
        if (search != null && !search.isEmpty()) {
            parents = userRepository.findByRoleAndSearchTerm(Role.PARENT, search, pageable);
        } else {
            parents = userRepository.findByRole(Role.PARENT, pageable);
        }
        return parents.map(this::convertToAdminParentResponse);
    }

    public AdminParentResponse getParentByIdForAdmin(Long parentId) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable avec l'ID: " + parentId));
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent");
        }
        List<Child> children = childRepository.findByParent(parent);
        return convertToAdminParentResponse(parent, children, null);
    }

    @Transactional
    public AdminParentResponse updateParent(Long parentId, AdminUpdateParentRequest request, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));

        if (request.getPhone() != null && !request.getPhone().equals(parent.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new Exception("Un parent avec ce téléphone existe déjà");
            }
            parent.setPhone(request.getPhone());
            parent.setUsername(request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().equals(parent.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new Exception("Un utilisateur avec cet email existe déjà");
            }
            parent.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) parent.setFirstName(request.getFirstName());
        if (request.getLastName() != null) parent.setLastName(request.getLastName());
        if (request.getActive() != null) parent.setActive(request.getActive());

        if (request.getExpirationDate() != null) {
            parent.setAccountExpirationDate(request.getExpirationDate());
            log.info("📅 Date d'expiration mise à jour: {}", request.getExpirationDate());
        } else if (parent.getAccountExpirationDate() == null) {
            LocalDate defaultExpiration = LocalDate.now().plusMonths(1);
            parent.setAccountExpirationDate(defaultExpiration);
            log.info("📅 Date d'expiration par défaut appliquée: {}", defaultExpiration);
        }

        User updatedParent = userRepository.save(parent);
        log.info("✅ Parent mis à jour: {}", updatedParent.getPhone());
        return convertToAdminParentResponse(updatedParent);
    }

    @Transactional
    public AdminChildResponse addChildToParent(Long parentId, AdminCreateChildRequest request, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent");
        }
        Child child = createChildEntity(request, parent);
        Child savedChild = childRepository.save(child);
        log.info("✅ Enfant ajouté: {} au parent {}", savedChild.getFirstName(), parent.getPhone());
        return convertToAdminChildResponse(savedChild, adminUsername);
    }

    public List<AdminChildResponse> getChildrenByParent(Long parentId) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        List<Child> children = childRepository.findByParent(parent);
        return children.stream()
                .map(child -> convertToAdminChildResponse(child, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteParent(Long parentId, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        List<Child> children = childRepository.findByParent(parent);
        childRepository.deleteAll(children);
        userRepository.delete(parent);
        log.info("✅ Parent supprimé: {}", parent.getPhone());
    }

    @Transactional
    public void deleteChild(Long childId, String adminUsername) throws Exception {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new Exception("Enfant introuvable"));
        childRepository.delete(child);
        log.info("✅ Enfant supprimé: {}", child.getFirstName());
    }

    @Transactional
    public List<AdminParentResponse> bulkImportParents(AdminBulkParentRequest request, String adminUsername) {
        return request.getParents().stream()
                .map(parentReq -> {
                    try {
                        return createParentWithChildren(parentReq, adminUsername);
                    } catch (Exception e) {
                        log.error("❌ Erreur import parent {}: {}", parentReq.getPhone(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public AdminStatsResponse getParentStats() {
        long totalParents = userRepository.countByRole(Role.PARENT);
        long activeParents = userRepository.countByRoleAndActive(Role.PARENT, true);
        long totalChildren = childRepository.count();
        long parentsWithChildren = userRepository.countParentsWithChildren();
        long expiredParents = userRepository.countByRoleAndAccountExpirationDateBefore(Role.PARENT, LocalDate.now());
        return new AdminStatsResponse(totalParents, activeParents, totalChildren, parentsWithChildren, expiredParents);
    }

    public List<AdminParentResponse> getExpiringParents(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysThreshold);
        List<User> parents = userRepository.findByRoleAndAccountExpirationDateBetween(Role.PARENT, today, futureDate);
        return parents.stream()
                .map(this::convertToAdminParentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public int deactivateExpiredParents() {
        List<User> expiredParents = userRepository.findByRoleAndAccountExpirationDateBeforeAndActive(
                Role.PARENT, LocalDate.now(), true);
        expiredParents.forEach(parent -> {
            parent.setActive(false);
            log.info("🔴 Parent désactivé (expiré): {}", parent.getPhone());
        });
        userRepository.saveAll(expiredParents);
        return expiredParents.size();
    }

    @Transactional
    public AdminParentResponse extendExpirationDate(Long parentId, LocalDate newExpirationDate, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent");
        }
        LocalDate oldDate = parent.getAccountExpirationDate();
        parent.setAccountExpirationDate(newExpirationDate);

        if (!parent.isActive() && newExpirationDate.isAfter(LocalDate.now())) {
            parent.setActive(true);
        }

        User updatedParent = userRepository.save(parent);
        log.info("📅 Expiration prolongée: {} -> {}", oldDate, newExpirationDate);
        return convertToAdminParentResponse(updatedParent);
    }

    @Transactional
    public AdminParentResponse addDaysToExpiration(Long parentId, int days, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent");
        }
        LocalDate oldDate = parent.getAccountExpirationDate();
        LocalDate newExpirationDate = (oldDate != null) ? oldDate.plusDays(days) : LocalDate.now().plusMonths(1);
        parent.setAccountExpirationDate(newExpirationDate);

        if (!parent.isActive() && newExpirationDate.isAfter(LocalDate.now())) {
            parent.setActive(true);
        }

        User updatedParent = userRepository.save(parent);
        log.info("📅 {} jours ajoutés: {} -> {}", days, oldDate, newExpirationDate);
        return convertToAdminParentResponse(updatedParent);
    }

    @Transactional
    public AdminParentResponse removeExpirationDate(Long parentId, String adminUsername) throws Exception {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new Exception("Parent introuvable"));
        if (parent.getRole() != Role.PARENT) {
            throw new Exception("L'utilisateur n'est pas un parent");
        }
        LocalDate oldDate = parent.getAccountExpirationDate();
        parent.setAccountExpirationDate(LocalDate.now().plusMonths(1));
        User updatedParent = userRepository.save(parent);
        log.info("📅 Expiration réinitialisée: {} -> {}", oldDate, updatedParent.getAccountExpirationDate());
        return convertToAdminParentResponse(updatedParent);
    }

    public List<AdminParentResponse> getParentsByDate(LocalDate startDate, LocalDate endDate) {
        List<User> parents = userRepository.findByRoleAndRegistrationDateBetween(
                Role.PARENT, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        return parents.stream()
                .map(this::convertToAdminParentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleParentStatus(Long parentId, boolean active) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé"));
        parent.setActive(active);
        userRepository.save(parent);
    }

    public User getParentDetails(Long parentId) {
        return userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé"));
    }

    @Transactional
    public User updateStatus(Long parentId, boolean newStatus) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé avec l'id: " + parentId));
        parent.setActive(newStatus);
        return userRepository.save(parent);
    }

    @Transactional
    public boolean toggleStatus(Long id) {
        Optional<User> parentOpt = userRepository.findById(id);
        if (parentOpt.isPresent()) {
            User parent = parentOpt.get();
            parent.setActive(!parent.isActive());
            userRepository.save(parent);
            return true;
        }
        return false;
    }


    private Child createChildEntity(AdminCreateChildRequest request, User parent) {
        Child child = new Child();
        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setSexe(request.getSexe());
        child.setAge(request.getAge());
        child.setParent(parent);
        return child;
    }

    private String generateTemporaryPassword() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private AdminParentResponse convertToAdminParentResponse(User user) {
        List<Child> children = childRepository.findByParent(user);
        return convertToAdminParentResponse(user, children, null);
    }

    private AdminParentResponse convertToAdminParentResponse(User user, List<Child> children, String createdBy) {
        List<AdminChildResponse> childrenDto = (children != null) ?
                children.stream()
                        .map(child -> convertToAdminChildResponse(child, createdBy))
                        .collect(Collectors.toList()) : List.of();

        String status;
        if (!user.isActive()) {
            status = "INACTIVE";
        } else if (user.getAccountExpirationDate() != null) {
            LocalDate today = LocalDate.now();
            if (user.getAccountExpirationDate().isBefore(today)) {
                status = "EXPIRED";
            } else if (user.getAccountExpirationDate().isBefore(today.plusDays(7))) {
                status = "EXPIRING_SOON";
            } else {
                status = "ACTIVE";
            }
        } else {
            status = "ACTIVE";
        }

        return new AdminParentResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getUsername(),
                user.isActive(),
                user.getIsVerified() != null ? user.getIsVerified() : false,
                user.getRegistrationDate(),
                user.getAccountExpirationDate(),
                null,
                childrenDto,
                childrenDto.size(),
                createdBy,
                null,
                status
        );
    }

    private AdminChildResponse convertToAdminChildResponse(Child child, String createdBy) {
        return new AdminChildResponse(
                child.getId(),
                child.getFirstName(),
                child.getLastName(),
                child.getSexe(),
                child.getAge(),
                child.getParent().getId(),
                child.getParent().getFirstName() + " " + child.getParent().getLastName(),
                null,
                null,
                LocalDateTime.now(),
                createdBy,
                child.isBlocked(),
                0,
                null
        );
    }

    public static class AdminStatsResponse {
        private long totalParents;
        private long activeParents;
        private long totalChildren;
        private long parentsWithChildren;
        private long expiredParents;

        public AdminStatsResponse(long totalParents, long activeParents, long totalChildren,
                                  long parentsWithChildren, long expiredParents) {
            this.totalParents = totalParents;
            this.activeParents = activeParents;
            this.totalChildren = totalChildren;
            this.parentsWithChildren = parentsWithChildren;
            this.expiredParents = expiredParents;
        }

        public long getTotalParents() { return totalParents; }
        public long getActiveParents() { return activeParents; }
        public long getTotalChildren() { return totalChildren; }
        public long getParentsWithChildren() { return parentsWithChildren; }
        public long getExpiredParents() { return expiredParents; }
    }

    public List<User> getParentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return userRepository.findByRoleAndRegistrationDateBetween(
                Role.PARENT,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
    }

    public List<User> getParentsByMonthYear(int month, int year) {
        return userRepository.findParentsByMonthAndYear(month, year);
    }


    public Page<AdminParentResponse> getParentsByDate(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Page<User> parents = userRepository.findByRegistrationDateBetweenAndRole(
                startDateTime, endDateTime, Role.PARENT, pageable
        );

        return parents.map(this::convertToAdminParentResponse);
    }
}
