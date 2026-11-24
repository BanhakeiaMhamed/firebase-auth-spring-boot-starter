package com.example.firebaseauth;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Résout la valeur de {@code firebase.credentials} en flux de lecture.
 * <p>
 * Formats acceptés :
 * <ul>
 *   <li>un <b>JSON en ligne</b> (valeur commençant par {@code {}), pratique avec une variable
 *       d'environnement ou un secret Docker/Kubernetes : {@code FIREBASE_CREDENTIALS='{"type":...}'} ;</li>
 *   <li>{@code classpath:chemin/fichier.json} ;</li>
 *   <li>un chemin de fichier, absolu ou relatif au <b>répertoire de travail</b>.</li>
 * </ul>
 */
final class FirebaseCredentialsLoader {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private FirebaseCredentialsLoader() {
    }

    static InputStream open(String spec) throws IOException {
        if (spec == null || spec.isBlank()) {
            throw new IllegalStateException(
                    "firebase.credentials n'est pas défini (variable FIREBASE_CREDENTIALS).");
        }
        String value = spec.trim();

        if (isInlineJson(value)) {
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        if (value.startsWith(CLASSPATH_PREFIX)) {
            Resource resource = new ClassPathResource(value.substring(CLASSPATH_PREFIX.length()));
            if (!resource.exists()) {
                throw new FileNotFoundException("Credentials Firebase introuvables dans le classpath : " + value);
            }
            return resource.getInputStream();
        }

        Path path = Paths.get(value);
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(
                    "Fichier de credentials Firebase introuvable : " + path.toAbsolutePath()
                            + " (répertoire de travail : " + Paths.get("").toAbsolutePath()
                            + "). Renseignez firebase.credentials / FIREBASE_CREDENTIALS avec un chemin absolu,"
                            + " un 'classpath:...' ou le contenu JSON du compte de service.");
        }
        return Files.newInputStream(path);
    }

    /** Description sûre pour les logs : n'expose jamais le contenu d'un JSON en ligne. */
    static String describe(String spec) {
        if (spec == null || spec.isBlank()) {
            return "(non défini)";
        }
        return isInlineJson(spec.trim()) ? "JSON en ligne (contenu masqué)" : spec.trim();
    }

    private static boolean isInlineJson(String value) {
        return value.startsWith("{");
    }
}
