package tn.example.backdeclitech.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.example.backdeclitech.presence_managment.entities.Presence;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String username;
    private String lastName;
    private Boolean isVerified = false;
    private String firstName;
    private String VerificationCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number")
    private String phone;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    private Role role;

    private boolean active = true;
    private LocalDateTime registrationDate;

    @Column(name = "code_expiration_date")
    private LocalDateTime codeExpirationDate;

    @Column(name = "account_expiration_date")
    private LocalDate accountExpirationDate;

    // ✅ CORRECTION : Ajouter @JoinTable
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_module",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<Module> module;

    @ManyToOne
    private CoBuildSpace coBuildSpace;

    @OneToMany(mappedBy = "parent")
    private List<FeedBack> feedBacks;

    @OneToMany(mappedBy = "organizingTeam")
    private List<Presence> presences;

    @OneToMany(mappedBy = "parent")
    private List<Reclamation> reclamations;

    @OneToMany(mappedBy = "parent")
    private List<Reservation> reservationList;

    @OneToMany(mappedBy = "organizingTeam")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "instructor")
    private List<TrackingSheet> trackingSheets;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Token> tokens;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private List<Child> children;

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.getRole()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        if (accountExpirationDate == null) {
            return true;
        }
        return !LocalDate.now().isAfter(accountExpirationDate);
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(@NotBlank(message = "Password is required")
                            @Size(min = 6, message = "Password must be at least 6 characters long")
                            String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    // Méthodes utilitaires pour l'expiration du compte
    public boolean isAccountExpired() {
        if (accountExpirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(accountExpirationDate);
    }

    public boolean isAccountExpiringSoon(int days) {
        if (accountExpirationDate == null) {
            return false;
        }
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return accountExpirationDate.isAfter(LocalDate.now()) &&
                accountExpirationDate.isBefore(futureDate);
    }

    public long getDaysUntilExpiration() {
        if (accountExpirationDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), accountExpirationDate);
    }

    // Méthodes utilitaires pour le code SMS
    public boolean isCodeExpired() {
        if (codeExpirationDate == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(codeExpirationDate);
    }

    public boolean isCodeValid() {
        return codeExpirationDate != null && LocalDateTime.now().isBefore(codeExpirationDate);
    }

    public LocalDate getAccountExpirationDate() {
        return accountExpirationDate;
    }

    public void setAccountExpirationDate(LocalDate accountExpirationDate) {
        this.accountExpirationDate = accountExpirationDate;
    }
}