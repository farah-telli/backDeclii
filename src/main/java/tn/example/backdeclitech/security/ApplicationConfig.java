package tn.example.backdeclitech.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        return identifier -> {
            System.out.println("Tentative de recherche d'utilisateur avec identifiant: " + identifier);

            Optional<User> userOptional = repository.findByEmail(identifier);

            if (userOptional.isEmpty()) {
                System.out.println("Utilisateur non trouvé par email, tentative par téléphone");
                userOptional = repository.findByPhone(identifier);
            }

            if (userOptional.isEmpty()) {
                System.out.println("Utilisateur non trouvé par téléphone, tentative par username");
                userOptional = repository.findByUsername(identifier);
            }

            if (userOptional.isEmpty()) {
                System.out.println("Utilisateur non trouvé avec l'identifiant: " + identifier);
                throw new UsernameNotFoundException("User not found with identifier: " + identifier);
            }

            System.out.println("Utilisateur trouvé : " + userOptional.get().getUsername());
            return userOptional.get();
        };
    }
}