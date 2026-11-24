package com.example.firebaseauth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * Filtre stateless : vérifie le Bearer token Firebase et peuple le SecurityContext.
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseService firebaseService;
    private final FirebaseAuthProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public FirebaseAuthenticationFilter(FirebaseService firebaseService,
                                        FirebaseAuthProperties properties) {
        this.firebaseService = firebaseService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                FirebaseToken decodedToken = firebaseService.verifyToken(token);
                String uid = decodedToken.getUid();
                Collection<SimpleGrantedAuthority> authorities =
                        firebaseService.extractAuthorities(decodedToken);

                UserDetails userDetails = new User(uid, "", authorities);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (FirebaseAuthException e) {
                logger.error("Firebase authentication failed", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String pattern : properties.getAuth().getPublicPaths()) {
            if (pathMatcher.match(pattern, path) || path.startsWith(stripAntSuffix(pattern))) {
                return true;
            }
        }
        return false;
    }

    /** Convertit un pattern Ant « /api/auth/** » en préfixe simple « /api/auth ». */
    private static String stripAntSuffix(String pattern) {
        if (pattern.endsWith("/**")) {
            return pattern.substring(0, pattern.length() - 3);
        }
        if (pattern.endsWith("/*")) {
            return pattern.substring(0, pattern.length() - 2);
        }
        return pattern;
    }
}
