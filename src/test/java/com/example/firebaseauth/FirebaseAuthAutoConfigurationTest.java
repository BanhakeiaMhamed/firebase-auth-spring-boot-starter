package com.example.firebaseauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class FirebaseAuthAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FirebaseAuthAutoConfiguration.class));

    /** Initialiseur neutralisé : évite d'appeler le vrai SDK Firebase dans ces tests. */
    @Configuration(proxyBeanMethods = false)
    static class NoOpInitializer {
        @Bean
        FirebaseInitializer firebaseInitializer(FirebaseAuthProperties properties) {
            return new FirebaseInitializer(properties) {
                @Override
                public void initialize() {
                    // volontairement vide
                }
            };
        }
    }

    @Test
    void autoConfiguration_isRegisteredInImportsFile() {
        // Garde-fou : sans ce fichier (absent du classpath ou mal nommé), le starter ne
        // s'active jamais chez le consommateur et ses beans sont « introuvables ».
        assertThat(ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates())
                .contains(FirebaseAuthAutoConfiguration.class.getName());
    }

    @Test
    void registersAllBeansByDefault() {
        runner.withUserConfiguration(NoOpInitializer.class).run(context -> {
            assertThat(context).hasSingleBean(FirebaseAuthProperties.class);
            assertThat(context).hasSingleBean(FirebaseInitializer.class);
            assertThat(context).hasSingleBean(FirebaseService.class);
            assertThat(context).hasSingleBean(FirebaseAuthenticationFilter.class);
            assertThat(context).hasSingleBean(FirebaseSecurityConfigurer.class);
        });
    }

    @Test
    void firebaseEnabledFalse_disablesEverything() {
        runner.withPropertyValues("firebase.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(FirebaseInitializer.class);
            assertThat(context).doesNotHaveBean(FirebaseService.class);
            assertThat(context).doesNotHaveBean(FirebaseAuthenticationFilter.class);
            assertThat(context).doesNotHaveBean(FirebaseSecurityConfigurer.class);
        });
    }

    @Test
    void bindsFirebaseProperties() {
        runner.withUserConfiguration(NoOpInitializer.class)
                .withPropertyValues(
                        "firebase.credentials=classpath:firebase-test-credentials.json",
                        "firebase.auth.public-paths=/public/**,/ping",
                        "firebase.auth.default-role=lecteur")
                .run(context -> {
                    FirebaseAuthProperties properties = context.getBean(FirebaseAuthProperties.class);
                    assertThat(properties.getCredentials()).isEqualTo("classpath:firebase-test-credentials.json");
                    assertThat(properties.getAuth().getPublicPaths()).containsExactly("/public/**", "/ping");
                    assertThat(properties.getAuth().getDefaultRole()).isEqualTo("lecteur");
                });
    }

    @Test
    void userDefinedBeanBacksOffTheDefaultOne() {
        FirebaseService custom = new FirebaseService(new FirebaseAuthProperties());
        runner.withUserConfiguration(NoOpInitializer.class)
                .withBean(FirebaseService.class, () -> custom)
                .run(context -> assertThat(context.getBean(FirebaseService.class)).isSameAs(custom));
    }

    @Test
    void defaultInitializer_failsClearlyWhenCredentialsAreMissing() {
        runner.withPropertyValues("firebase.credentials=definitely-missing-credentials.json")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .hasRootCauseInstanceOf(FileNotFoundException.class)
                        .hasStackTraceContaining("definitely-missing-credentials.json"));
    }
}
