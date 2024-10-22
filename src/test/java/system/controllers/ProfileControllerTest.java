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
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
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
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedPassword");

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊")
                        .param("password", "newpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"))
                .andExpect(model().attribute("success", "Profile updated successfully"));

        verify(passwordEncoder).encode("newpassword");
        verify(userService).save(argThat(user ->
                user.getPassword().equals("encodedPassword") &&
                        user.getEmail().equals("test@example.com") &&
                        user.getFirstName().equals("Test") &&
                        user.getLastName().equals("User") &&
                        user.getPhone().equals("1234567890") &&
                        user.getProfileEmoji().equals("😊")
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithoutPassword() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setPassword("oldpassword");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(passwordEncoder, never()).encode(any());
        verify(userService).save(argThat(user ->
                user.getPassword().equals("oldpassword")
        ));
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

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithValidPhoneNumber() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(userService).save(argThat(user ->
                user.getPhone().equals("1234567890")
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithNullPhone() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(userService).save(argThat(user ->
                user.getPhone() == null || user.getPhone().isEmpty()
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyPhone() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(userService).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithNonDigitPhone() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "123abc4567")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Phone number must be 10 digits"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyEmail() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "All fields are required"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyFirstName() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "All fields are required"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyLastName() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "All fields are required"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyPassword() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setPassword("oldpassword");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(passwordEncoder, never()).encode(any());
        verify(userService).save(argThat(user ->
                user.getPassword().equals("oldpassword")
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithNullPassword() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setPassword("oldpassword");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("success"));

        verify(passwordEncoder, never()).encode(any());
        verify(userService).save(argThat(user ->
                user.getPassword().equals("oldpassword")
        ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithNonNullInvalidPhone() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "123456")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Phone number must be 10 digits"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithInvalidPhoneNumber() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "123")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Phone number must be 10 digits"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithLongEmoji() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊😊😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Profile emoji must be a single character"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfileWithEmptyRequiredFields() throws Exception {
        User currentUser = new User();
        currentUser.setUsername("testuser");
        when(userService.getCurrentlyLoggedInUser()).thenReturn(currentUser);

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("email", "")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("phone", "1234567890")
                        .param("profileEmoji", "😊"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "All fields are required"));

        verify(userService, never()).save(any(User.class));
    }
}