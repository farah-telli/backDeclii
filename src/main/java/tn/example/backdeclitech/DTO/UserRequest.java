package tn.example.backdeclitech.DTO;

import lombok.*;
import tn.example.backdeclitech.entities.Role;

@Getter
@Setter
@NoArgsConstructor
@Data
public class UserRequest {
        private Long id;
        private String firstName;
        private String lastName;
        private String username;
        private String email;
        private String phone;
        private String password;
        private Role role;
        private Boolean isVerified;
        private boolean active;
        private String registrationDate;
        private String expirationDate;

        private String coBuildSpaceName;

        public UserRequest(Long id, String firstName, String lastName, String username,
                           String email, String phone, Role role, Boolean isVerified,
                           boolean active, String registrationDate, String expirationDate,
                           String coBuildSpaceName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.isVerified = isVerified;
            this.active = active;
            this.registrationDate = registrationDate;
            this.expirationDate = expirationDate;
            this.coBuildSpaceName = coBuildSpaceName;
        }

        public String getCoBuildSpaceName() {
            return coBuildSpaceName;
        }
    }
