package tn.example.backdeclitech.services;

import tn.example.backdeclitech.DTO.AuthenticationResponse;
import tn.example.backdeclitech.DTO.CodeVerificationRequest;
import tn.example.backdeclitech.DTO.EmailLoginRequest;
import tn.example.backdeclitech.DTO.PhoneLoginRequest;
import tn.example.backdeclitech.DTO.RefreshTokenRequest;

public interface IAuthenticationService {
    public AuthenticationResponse authenticateParent(PhoneLoginRequest request) throws Exception;
    public AuthenticationResponse authenticateOthers(EmailLoginRequest request) throws Exception;
    public AuthenticationResponse authenticateParentWithCode(CodeVerificationRequest request) throws Exception;
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws Exception;
    public void logout(String token) throws Exception;
}