package system.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import system.models.User;
import system.services.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("1234567890");
        testUser.setProfileEmoji("😊");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProfilePage() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile() throws Exception {
        when(userService.getCurrentlyLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "newemail@example.com")
                        .param("firstName", "NewFirst")
                        .param("lastName", "NewLast")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "🎉"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));

        verify(userService).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithPassword() throws Exception {
        when(userService.getCurrentlyLoggedInUser()).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "newemail@example.com")
                        .param("firstName", "NewFirst")
                        .param("lastName", "NewLast")
                        .param("phone", "1234567890")
                        .param("password", "newpassword")
                        .param("profileEmoji", "🚀"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));

        verify(userService).save(any(User.class));
        verify(passwordEncoder).encode("newpassword");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithoutPassword() throws Exception {
        when(userService.getCurrentlyLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/profile/update")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "newemail@example.com")
                        .param("firstName", "NewFirst")
                        .param("lastName", "NewLast")
                        .param("phone", "9876543210")
                        .param("profileEmoji", "🎉"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));

        verify(userService).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileEmoji() throws Exception {
        when(userService.getCurrentlyLoggedInUser()).thenReturn(testUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", testUser.getEmail())
                        .param("firstName", testUser.getFirstName())
                        .param("lastName", testUser.getLastName())
                        .param("phone", testUser.getPhone() != null ? testUser.getPhone() : "")
                        .param("profileEmoji", "🌈"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));

        verify(userService).save(any(User.class));
    }
}