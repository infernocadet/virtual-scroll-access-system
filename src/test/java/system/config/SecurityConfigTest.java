package system.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.services.CustomUserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private CustomUserDetailsService mockUserDetailsService;

    @BeforeEach
    void setUp() {
        mockUserDetailsService = mock(CustomUserDetailsService.class);
        securityConfig = new SecurityConfig(mockUserDetailsService);
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
    }
}