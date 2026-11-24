package com.example.firebaseauth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseCredentialsLoaderTest {

    private static final String JSON = "{\"type\":\"service_account\",\"private_key\":\"SECRET-MARKER\"}";

    @Test
    void open_readsInlineJson() throws Exception {
        try (InputStream in = FirebaseCredentialsLoader.open("  " + JSON + "  ")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(JSON);
        }
    }

    @Test
    void open_readsAbsoluteFilePath(@TempDir Path dir) throws Exception {
        Path file = Files.writeString(dir.resolve("creds.json"), JSON);

        try (InputStream in = FirebaseCredentialsLoader.open(file.toString())) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(JSON);
        }
    }

    @Test
    void open_readsClasspathResource() throws Exception {
        try (InputStream in = FirebaseCredentialsLoader.open("classpath:firebase-test-credentials.json")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).contains("service_account");
        }
    }

    @Test
    void open_missingFile_explainsWhereItLooked(@TempDir Path dir) {
        String missing = dir.resolve("absent.json").toString();

        assertThatThrownBy(() -> FirebaseCredentialsLoader.open(missing))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("absent.json")
                .hasMessageContaining("répertoire de travail")
                .hasMessageContaining("FIREBASE_CREDENTIALS");
    }

    @Test
    void open_missingClasspathResource_isReported() {
        assertThatThrownBy(() -> FirebaseCredentialsLoader.open("classpath:nope.json"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("classpath:nope.json");
    }

    @Test
    void open_blank_isRejected() {
        assertThatThrownBy(() -> FirebaseCredentialsLoader.open("  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("firebase.credentials");
    }

    @Test
    void describe_neverLeaksInlineJson() {
        assertThat(FirebaseCredentialsLoader.describe(JSON))
                .doesNotContain("SECRET-MARKER")
                .contains("masqué");
        assertThat(FirebaseCredentialsLoader.describe("/etc/firebase.json")).isEqualTo("/etc/firebase.json");
    }
}
