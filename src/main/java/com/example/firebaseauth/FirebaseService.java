package com.example.firebaseauth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service générique de vérification de tokens Firebase et extraction des rôles
 * depuis les custom claims.
 */
public class FirebaseService {

    private final FirebaseAuthProperties properties;

    public FirebaseService(FirebaseAuthProperties properties) {
        this.properties = properties;
    }

    public FirebaseToken verifyToken(String token) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(token);
    }

    /**
     * Extrait les autorités Spring Security depuis le claim « roles » du token.
     */
    public Collection<SimpleGrantedAuthority> extractAuthorities(FirebaseToken decodedToken) {
        Object rolesClaim = decodedToken.getClaims().get("roles");
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (rolesClaim instanceof List<?> rolesList) {
            for (Object roleObj : rolesList) {
                String role = roleObj.toString().trim().toUpperCase();
                if (!role.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }

        if (authorities.isEmpty()) {
            String defaultRole = properties.getAuth().getDefaultRole().trim().toUpperCase();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + defaultRole));
        }

        return authorities;
    }
}
