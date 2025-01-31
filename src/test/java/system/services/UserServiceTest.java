package system.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.models.User;
import system.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        User result = userService.findByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    void testUserExists() {
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));

        boolean result = userService.userExists("testuser");

        assertTrue(result);
    }

    @Test
    void testUserExists_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        boolean result = userService.userExists("nonexistent");

        assertFalse(result);
    }

    @Test
    void testSaveNewUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.save(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(newUser);
    }

    @Test
    void testSaveExistingUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUser);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetCurrentlyLoggedInUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn("testuser");
        when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.getCurrentlyLoggedInUser();

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetCurrentlyLoggedInUserNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn("nonexistentuser");
        when(userRepository.findByUsernameIgnoreCase("nonexistentuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentlyLoggedInUser());
    }

    @Test
    void testSaveNewUserWithAdminRole() {
        User newAdminUser = new User();
        newAdminUser.setUsername("newadmin");
        newAdminUser.setPassword("adminpassword");
        newAdminUser.setAdmin(true);

        when(passwordEncoder.encode("adminpassword")).thenReturn("encodedAdminPassword");
        when(userRepository.save(any(User.class))).thenReturn(newAdminUser);

        User result = userService.save(newAdminUser);

        assertNotNull(result);
        assertEquals("newadmin", result.getUsername());
        assertTrue(result.isAdmin());
        verify(passwordEncoder).encode("adminpassword");
        verify(userRepository).save(newAdminUser);
    }
}