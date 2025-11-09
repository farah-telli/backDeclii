package tn.example.backdeclitech.aspect;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tn.example.backdeclitech.entities.AuditActionType;
import tn.example.backdeclitech.entities.AuditEntityType;
import tn.example.backdeclitech.entities.ModuleSession;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.services.AuditLogService;

/**
 * Aspect pour capturer automatiquement les actions CRUD et créer des logs d'audit
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    /**
     * Capturer les créations d'utilisateurs
     */
    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.createUser(..))",
            returning = "result"
    )
    public void afterUserCreation(JoinPoint joinPoint, User result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.CREATE,
                AuditEntityType.USER,
                result.getId(),
                result.getFirstName() + " " + result.getLastName(),
                currentUser,
                null,
                result,
                "Création d'un nouvel utilisateur",
                request
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.updateUser(..)) && args(userId, updateData)",
            returning = "result"
    )
    public void afterUserUpdate(JoinPoint joinPoint, Long userId, Object updateData, User result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.UPDATE,
                AuditEntityType.USER,
                result.getId(),
                result.getFirstName() + " " + result.getLastName(),
                currentUser,
                joinPoint.getArgs()[1], // oldValue (peut nécessiter ajustement)
                result,
                "Modification d'un utilisateur",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.deleteUser(..)) && args(userId)"
    )
    public void afterUserDeletion(JoinPoint joinPoint, Long userId) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.DELETE,
                AuditEntityType.USER,
                userId,
                "Utilisateur #" + userId,
                currentUser,
                null,
                null,
                "Suppression d'un utilisateur",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.bulkActivate(..)) && args(userIds)",
            returning = "result"
    )
    public void afterBulkActivation(JoinPoint joinPoint, java.util.List<Long> userIds, Object result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createBulkAuditLog(
                AuditActionType.BULK_ACTIVATE,
                AuditEntityType.USER,
                userIds.size(),
                currentUser,
                "Activation groupée de " + userIds.size() + " utilisateur(s)",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.bulkDeactivate(..)) && args(userIds)",
            returning = "result"
    )
    public void afterBulkDeactivation(JoinPoint joinPoint, java.util.List<Long> userIds, Object result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createBulkAuditLog(
                AuditActionType.BULK_DEACTIVATE,
                AuditEntityType.USER,
                userIds.size(),
                currentUser,
                "Désactivation groupée de " + userIds.size() + " utilisateur(s)",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.UserService.bulkDelete(..)) && args(userIds)",
            returning = "result"
    )
    public void afterBulkDeletion(JoinPoint joinPoint, java.util.List<Long> userIds, Object result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createBulkAuditLog(
                AuditActionType.BULK_DELETE,
                AuditEntityType.USER,
                userIds.size(),
                currentUser,
                "Suppression groupée de " + userIds.size() + " utilisateur(s)",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech.services.AuthenticationService.authenticateOthers(..))",
            returning = "result"
    )
    public void afterLogin(JoinPoint joinPoint, Object result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        if (currentUser != null) {
            auditLogService.createAuditLog(
                    AuditActionType.LOGIN,
                    AuditEntityType.USER,
                    currentUser.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName(),
                    currentUser,
                    null,
                    null,
                    "Connexion utilisateur",
                    request
            );
        }
    }


    @AfterReturning(
            pointcut = "execution(* com.declitech.backend.service.AuthService.logout(..))"
    )
    public void afterLogout(JoinPoint joinPoint) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        if (currentUser != null) {
            auditLogService.createAuditLog(
                    AuditActionType.LOGOUT,
                    AuditEntityType.USER,
                    currentUser.getId(),
                    currentUser.getFirstName() + " " + currentUser.getLastName(),
                    currentUser,
                    null,
                    null,
                    "Déconnexion utilisateur",
                    request
            );
        }
    }


    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech.services.ModuleService.createModule(..))",
            returning = "result"
    )
    public void afterModuleCreation(JoinPoint joinPoint, tn.example.backdeclitech.entities.Module result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.CREATE,
                AuditEntityType.MODULE,
                result.getId(),
                result.getTitle(),
                currentUser,
                null,
                result,
                "Création d'un nouveau module",
                request
        );
    }

    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech..services.ModuleService.updateModule(..))",
            returning = "result"
    )
    public void afterModuleUpdate(JoinPoint joinPoint, tn.example.backdeclitech.entities.Module result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.UPDATE,
                AuditEntityType.MODULE,
                result.getId(),
                result.getTitle(),
                currentUser,
                null,
                result,
                "Modification d'un module",
                request
        );
    }

    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech..services.ModuleService.deleteModule(..)) && args(moduleId)"
    )
    public void afterModuleDeletion(JoinPoint joinPoint, Long moduleId) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.DELETE,
                AuditEntityType.MODULE,
                moduleId,
                "Module #" + moduleId,
                currentUser,
                null,
                null,
                "Suppression d'un module",
                request
        );
    }


    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech..services.ModuleSessionService.createSession(..))",
            returning = "result"
    )
    public void afterSessionCreation(JoinPoint joinPoint, ModuleSession result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.CREATE,
                AuditEntityType.SESSION,
                result.getId(),
                result.getModule() != null ? result.getModule().getTitle() : "Session",
                currentUser,
                null,
                result,
                "Création d'une nouvelle session",
                request
        );
    }

    @AfterReturning(
            pointcut = "execution(* tn.example.backdeclitech.services.ModuleSessionService.updateAnnuleStatus(..))",
            returning = "result"
    )
    public void afterSessionUpdate(JoinPoint joinPoint, ModuleSession result) {
        User currentUser = getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        auditLogService.createAuditLog(
                AuditActionType.UPDATE,
                AuditEntityType.SESSION,
                result.getId(),
                result.getModule() != null ? result.getModule().getTitle() : "Session",
                currentUser,
                null,
                result,
                "Modification d'une session",
                request
        );
    }


    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }
}