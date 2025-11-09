package tn.example.backdeclitech.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        return userRepository.findByEmail(loginIdentifier)
                .or(() -> userRepository.findByPhone(loginIdentifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with identifier: " + loginIdentifier));
    }
}
