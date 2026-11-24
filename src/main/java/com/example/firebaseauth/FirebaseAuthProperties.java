package com.example.firebaseauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "firebase")
public class FirebaseAuthProperties {

    /** Chemin vers le fichier JSON des credentials Firebase Admin SDK. */
    private String credentials = "firebase-credentials.json";

    private final Auth auth = new Auth();

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Auth {

        /** Chemins HTTP exemptés du filtre Firebase (Ant-style ou préfixe simple). */
        private List<String> publicPaths = new ArrayList<>(List.of(
                "/api/auth/**",
                "/api/health"
        ));

        /** Rôle Spring Security par défaut si aucun claim « roles » n'est présent. */
        private String defaultRole = "USER";

        public List<String> getPublicPaths() {
            return publicPaths;
        }

        public void setPublicPaths(List<String> publicPaths) {
            this.publicPaths = publicPaths;
        }

        public String getDefaultRole() {
            return defaultRole;
        }

        public void setDefaultRole(String defaultRole) {
            this.defaultRole = defaultRole;
        }
    }
}
