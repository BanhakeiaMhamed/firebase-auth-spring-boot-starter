package com.example.firebaseauth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Applique à une {@link HttpSecurity} le socle commun de l'authentification Firebase :
 * CSRF désactivé, sessions stateless, filtre Bearer Firebase, réponses 401/403 en JSON et
 * accès libre aux chemins listés dans {@code firebase.auth.public-paths}.
 * <p>
 * Les règles d'autorisation propres à l'application (rôles par URL, {@code anyRequest()}...)
 * restent dans l'application, à ajouter après l'appel :
 * <pre>{@code
 * @Bean
 * SecurityFilterChain apiChain(HttpSecurity http, FirebaseSecurityConfigurer firebase) throws Exception {
 *     firebase.configure(http.securityMatcher("/api/**"));
 *     http.authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *             .anyRequest().authenticated());
 *     return http.build();
 * }
 * }</pre>
 */
public class FirebaseSecurityConfigurer {

    private final FirebaseAuthenticationFilter filter;
    private final FirebaseAuthProperties properties;

    public FirebaseSecurityConfigurer(FirebaseAuthenticationFilter filter, FirebaseAuthProperties properties) {
        this.filter = filter;
        this.properties = properties;
    }

    public HttpSecurity configure(HttpSecurity http) throws Exception {
        String[] publicPaths = properties.getAuth().getPublicPaths().toArray(String[]::new);

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(publicPaths).permitAll())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "{\"error\":\"Unauthorized - Invalid or missing token\"}"))
                        .accessDeniedHandler((request, response, e) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                                        "{\"error\":\"Forbidden\"}")));
        return http;
    }

    private static void writeJson(HttpServletResponse response, int status, String body) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(body);
    }
}
