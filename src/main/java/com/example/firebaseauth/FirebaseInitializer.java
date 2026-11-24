package com.example.firebaseauth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initialise Firebase Admin SDK au démarrage à partir de {@code firebase.credentials}
 * (JSON en ligne, {@code classpath:...} ou chemin de fichier — voir {@link FirebaseCredentialsLoader}).
 */
public class FirebaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(FirebaseInitializer.class);

    private final FirebaseAuthProperties properties;

    public FirebaseInitializer(FirebaseAuthProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase déjà initialisé, skip.");
            return;
        }

        String source = FirebaseCredentialsLoader.describe(properties.getCredentials());
        log.info("Initialisation Firebase depuis : {}", source);

        try (InputStream serviceAccount = FirebaseCredentialsLoader.open(properties.getCredentials())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialisé avec succès.");
        } catch (IOException e) {
            log.error("Impossible de charger les credentials Firebase ({}) : {}", source, e.getMessage());
            throw new IllegalStateException("Firebase initialization failed : " + e.getMessage(), e);
        }
    }
}
