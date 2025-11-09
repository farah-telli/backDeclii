package tn.example.backdeclitech.security;


public class SecurityConstants {
    public static final String SECRET_STRING = "ma-cle-tres-secrete-pour-jwt-qui-doit-etre-longue-1234";
    public static final long EXPIRATION_TIME = 3_600_000; // 1 hour in ms
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 604_800_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}
