package tn.example.backdeclitech.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.DTO.*;
import tn.example.backdeclitech.Util.JWTUtils;
import tn.example.backdeclitech.entities.Role;
import tn.example.backdeclitech.entities.Token;
import tn.example.backdeclitech.entities.TokenType;
import tn.example.backdeclitech.entities.User;
import tn.example.backdeclitech.repositories.TokenRepository;
import tn.example.backdeclitech.repositories.UserRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements IAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository repository;
    private final JWTUtils jwtService;
    private final TokenRepository tokenRepository;

    @Override
    public AuthenticationResponse authenticateParent(PhoneLoginRequest request) throws Exception {
        if (ParentAuthenticationRequestValidator(request)) {
            User parent = repository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            if (parent.getRole() != Role.PARENT) {
                throw new BadCredentialsException("Access denied: Phone login is only for PARENT role");
            }
            
            if (!parent.isAccountNonLocked()) {
                throw new Exception("Your account is locked");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhone(),
                            request.getPassword()
                    ));

            if (authentication.isAuthenticated()) {
                return generateTokenResponse(parent);
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }
        }
        return null;
    }

    @Override
    public AuthenticationResponse authenticateOthers(EmailLoginRequest request) throws Exception {
        if (OthersAuthenticationRequestValidator(request)) {
            User user = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            if (user.getRole() == Role.PARENT) {
                throw new BadCredentialsException("Access denied: Parents must use phone login");
            }
            
            if (!user.isAccountNonLocked()) {
                throw new Exception("Your account is locked");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    ));

            if (authentication.isAuthenticated()) {
                return generateTokenResponse(user);
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }
        }
        return null;
    }

    @Override
    public AuthenticationResponse authenticateParentWithCode(CodeVerificationRequest request) throws Exception {
        User parent = repository.findByPhone(request.getPhoneNumber())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (parent.getRole() != Role.PARENT) {
            throw new BadCredentialsException("Access denied: Code verification is only for PARENT role");
        }
        
        if (!parent.getVerificationCode().equals(request.getCode())) {
            throw new BadCredentialsException("Invalid verification code");
        }
        
        parent.setVerificationCode(null);
        repository.save(parent);
        
        return generateTokenResponse(parent);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws Exception {
        String refreshTokenString = request.getRefreshToken();
        
        if (jwtService.isTokenExpired(refreshTokenString)) {
            throw new Exception("Refresh token is expired");
        }
        
        String userEmail = jwtService.extractUsername(refreshTokenString);
        User user = repository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Token storedToken = tokenRepository.findByTokenAndTokenType(refreshTokenString, TokenType.REFRESH)
                .orElseThrow(() -> new Exception("Invalid refresh token"));
        
        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new Exception("Refresh token is not valid");
        }
        
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
        
        revokeAllUserTokensByType(user, TokenType.BEARER);
        
        saveUserToken(user, newAccessToken, TokenType.BEARER);
        saveUserToken(user, newRefreshToken, TokenType.REFRESH);
        
        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(String token) throws Exception {
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new Exception("Token not found"));
        
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
        
        revokeAllUserTokens(storedToken.getUser());
    }

    private AuthenticationResponse generateTokenResponse(User user) {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        revokeAllUserTokens(user);
        
        saveUserToken(user, jwtToken, TokenType.BEARER);
        saveUserToken(user, refreshToken, TokenType.REFRESH);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    private void saveUserToken(User user, String jwtToken, TokenType tokenType) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .insertionDate(new Date())
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void revokeAllUserTokensByType(User user, TokenType tokenType) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUserAndType(user.getId(), tokenType);
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public static boolean ParentAuthenticationRequestValidator(PhoneLoginRequest request) throws Exception {
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new Exception("Password is empty");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new Exception("Phone is empty");
        }
        return true;
    }

    public static boolean OthersAuthenticationRequestValidator(EmailLoginRequest request) throws Exception {
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new Exception("Password is empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new Exception("Email is empty");
        }
        return true;
    }
}