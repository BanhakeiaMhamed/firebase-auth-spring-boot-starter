package com.example.firebaseauth;

import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseServiceTest {

    @Test
    void extractAuthorities_readsRolesClaim() {
        FirebaseAuthProperties properties = new FirebaseAuthProperties();
        FirebaseService service = new FirebaseService(properties);

        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getClaims()).thenReturn(Map.of("roles", List.of("admin", "REDACTEUR")));

        Collection<SimpleGrantedAuthority> authorities = service.extractAuthorities(token);

        assertThat(authorities)
                .extracting(SimpleGrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_REDACTEUR");
    }

    @Test
    void extractAuthorities_fallsBackToDefaultRole() {
        FirebaseAuthProperties properties = new FirebaseAuthProperties();
        properties.getAuth().setDefaultRole("LECTEUR");
        FirebaseService service = new FirebaseService(properties);

        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getClaims()).thenReturn(Map.of());

        Collection<SimpleGrantedAuthority> authorities = service.extractAuthorities(token);

        assertThat(authorities)
                .extracting(SimpleGrantedAuthority::getAuthority)
                .containsExactly("ROLE_LECTEUR");
    }
}
