package com.example.firebaseauth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "com.google.firebase.FirebaseApp")
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FirebaseAuthProperties.class)
public class FirebaseAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    FirebaseInitializer firebaseInitializer(FirebaseAuthProperties properties) {
        return new FirebaseInitializer(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    FirebaseService firebaseService(FirebaseAuthProperties properties) {
        return new FirebaseService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    FirebaseAuthenticationFilter firebaseAuthenticationFilter(FirebaseService firebaseService,
                                                              FirebaseAuthProperties properties) {
        return new FirebaseAuthenticationFilter(firebaseService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    FirebaseSecurityConfigurer firebaseSecurityConfigurer(FirebaseAuthenticationFilter filter,
                                                          FirebaseAuthProperties properties) {
        return new FirebaseSecurityConfigurer(filter, properties);
    }
}
