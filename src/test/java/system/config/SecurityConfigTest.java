package system.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import system.services.CustomUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private CustomUserDetailsService mockUserDetailsService;
    private HttpSecurity mockHttpSecurity;

    @BeforeEach
    void setUp() {
        mockUserDetailsService = mock(CustomUserDetailsService.class);
        securityConfig = new SecurityConfig(mockUserDetailsService);
        mockHttpSecurity = mock(HttpSecurity.class, RETURNS_SELF);
    }

    @Test
    void testAuthenticationProvider() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        DaoAuthenticationProvider result = securityConfig.authenticationProvider(mockUserDetailsService, passwordEncoder);

        assertNotNull(result);
    }

    @Test
    void testIgnoringCustomizer() {
        var result = securityConfig.ignoringCustomizer();

        assertNotNull(result);
    }

    @Test
    void testPasswordEncoder() {
        var result = securityConfig.passwordEncoder();

        assertNotNull(result);
        assertTrue(result instanceof BCryptPasswordEncoder);
    }
}