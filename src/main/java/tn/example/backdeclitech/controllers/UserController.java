package tn.example.backdeclitech.controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.example.backdeclitech.DTO.InstructorDTO;
import tn.example.backdeclitech.DTO.ParentChildStatsDTO;
import tn.example.backdeclitech.DTO.UserDTO;
import tn.example.backdeclitech.DTO.UserRequest;
import tn.example.backdeclitech.Util.JWTUtils;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.Sentiment;
import tn.example.backdeclitech.entities.StatusReservation;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.FeedBackRepository;
import tn.example.backdeclitech.repositories.ReservationRepository;
import tn.example.backdeclitech.repositories.UserRepository;
import tn.example.backdeclitech.services.DashboardService;
import tn.example.backdeclitech.services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/instructors")
@CrossOrigin(origins = "http://localhost:4300")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private FeedBackRepository feedBackRepository;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private JWTUtils jwtUtils;

    @GetMapping("/getall")
    public ResponseEntity<List<InstructorDTO>> getAllInstructors() {
        List<InstructorDTO> instructorList = userService.getAllInstructorDTOs();
        return ResponseEntity.ok(instructorList);
    }

    @GetMapping("/get")
    public ResponseEntity<List<UserRequest>> getAllUsers() {
        List<UserRequest> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/parent")
    public ResponseEntity<Map<String, Long>> getParentStats() {
        return ResponseEntity.ok(userService.getParentStats());
    }

    @GetMapping(value = "/parents/children-stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ParentChildStatsDTO>> getParentChildStats() {
        List<ParentChildStatsDTO> stats = userService.getParentChildStats();
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/parents-status")
    public ResponseEntity<Map<String, Long>> getParentsStatusStats() {
        Role parentRole = Role.PARENT;

        long activeCount = userRepository.countByRoleAndActive(parentRole, true);
        long inactiveCount = userRepository.countByRoleAndActive(parentRole, false);

        Map<String, Long> response = Map.of(
                "active", activeCount,
                "inactive", inactiveCount
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations-status")
    public ResponseEntity<Map<String, Long>> getReservationsStatus() {

        Map<String, Long> response = Map.of(
                "RESERVED", reservationRepository.countByStatus(StatusReservation.RESERVED),
                "CANCELED", reservationRepository.countByStatus(StatusReservation.CANCELED),
                "COMPLETED", reservationRepository.countByStatus(StatusReservation.COMPLETED),
                "PENALIZED", reservationRepository.countByStatus(StatusReservation.PENALIZED)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feedback-sentiment")
    public ResponseEntity<Map<String, Long>> getFeedbackSentimentStats() {
        long positiveCount = feedBackRepository.countBySentiment(Sentiment.POSITIVE);
        long neutralCount  = feedBackRepository.countBySentiment(Sentiment.NEUTRAL);
        long negativeCount = feedBackRepository.countBySentiment(Sentiment.NEGATIVE);

        Map<String, Long> stats = new HashMap<>();
        stats.put("POSITIVE", positiveCount);
        stats.put("NEUTRAL", neutralCount);
        stats.put("NEGATIVE", negativeCount);

        return ResponseEntity.ok(stats);
    }
    @GetMapping(value = "/age-groups", produces = "application/json")
    public ResponseEntity<Map<String, Long>> getAgeGroupsStats() {
        return ResponseEntity.ok(dashboardService.getAgeGroupsStats());
    }

    @PostMapping("/creer")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/modifierUser/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Long userId = jwtUtils.extractUserId(token);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                UserDTO userDTO = UserDTO.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().name())
                        .username(user.getUsername())
                        .isVerified(user.getIsVerified())
                        .build();

                return ResponseEntity.ok(userDTO);
            }
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/bulk-activate")
    public ResponseEntity<Map<String, String>> bulkActivate(@RequestBody List<Long> userIds) {
        userService.bulkActivate(userIds);
        Map<String, String> response = new HashMap<>();
        response.put("message", userIds.size() + " utilisateur(s) activé(s) avec succès");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-deactivate")
    public ResponseEntity<Map<String, String>> bulkDeactivate(@RequestBody List<Long> userIds) {
        userService.bulkDeactivate(userIds);
        Map<String, String> response = new HashMap<>();
        response.put("message", userIds.size() + " utilisateur(s) désactivé(s) avec succès");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, String>> bulkDelete(@RequestBody List<Long> userIds) {
        userService.bulkDelete(userIds);
        Map<String, String> response = new HashMap<>();
        response.put("message", userIds.size() + " utilisateur(s) supprimé(s) avec succès");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/bulk-change-role")
    public ResponseEntity<Map<String, String>> bulkChangeRole(
            @RequestBody Map<String, Object> request
    ) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");
        String roleStr = (String) request.get("role");
        Role newRole = Role.valueOf(roleStr);

        userService.bulkChangeRole(userIds, newRole);

        Map<String, String> response = new HashMap<>();
        response.put("message", userIds.size() + " utilisateur(s) mis à jour avec le rôle " + newRole);
        return ResponseEntity.ok(response);
    }
}