package system.controllers;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void testGetRegister() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser
    void testPostRegister_Success() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password")
                        .param("email", "newuser@example.com")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }

    @Test
    @WithMockUser
    void testPostRegister_UserExists() throws Exception {
        when(userService.userExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("username", "existingUser")
                        .param("password", "password")
                        .param("email", "existing@example.com")
                        .param("firstName", "Existing")
                        .param("lastName", "User")
                        .param("phone", "1234567890")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username already exists"));

        verify(userService, never()).save(any());
    }

    @Test
    @WithMockUser
    void testPostRegister_EmptyUsername() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "")
                        .param("password", "password")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username or Password is empty"));

        verify(userService, never()).save(any());
    }

    @Test
    @WithMockUser
    void testPostRegister_InvalidEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password")
                        .param("email", "invalid-email")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Invalid email"));

        verify(userService, never()).save(any());
    }

    @Test
    @WithMockUser
    void testPostRegister_InvalidPhoneNumber() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password")
                        .param("email", "valid@example.com")
                        .param("phone", "123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Phone number must be 10 digits"));

        verify(userService, never()).save(any());
    }

    @Test
    void testPostRegister_UsernameWithSpecialCharacters() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "user@name")
                        .param("password", "password")
                        .param("email", "user@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Username can only contain letters, numbers, and spaces"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testPostRegister_UsernameTooLong() throws Exception {
        String longUsername = "a".repeat(256);
        mockMvc.perform(post("/register")
                        .param("username", longUsername)
                        .param("password", "password")
                        .param("email", "user@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Username must be less than 255 characters"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testPostRegister_PasswordTooShort() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "short")
                        .param("email", "newuser@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Password must be at least 8 characters long"));

        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testPostRegister_EmptyOptionalFields() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("password", "password")
                        .param("email", "newuser@example.com")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("phone", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any(User.class));
    }

    @Test
    void testPostRegister_NullUsername() throws Exception {
        mockMvc.perform(post("/register")
                        .param("password", "password")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username or Password is empty"));

        verify(userService, never()).save(any());
    }

    @Test
    void testPostRegister_NullPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username or Password is empty"));

        verify(userService, never()).save(any());
    }

    @Test
    void testPostRegister_NullEmail() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }

    @Test
    void testPostRegister_NonAlphanumericUsername() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "test!user")
                        .param("password", "password123")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username can only contain letters, numbers, and spaces"));

        verify(userService, never()).save(any());
    }

    @Test
    void testPostRegister_EmptyOptionalFieldsWithNullValues() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }

    @Test
    void testPostRegister_ValidEmailWithUpperCase() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "TEST@EXAMPLE.COM")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }

    @Test
    void testPostRegister_EmptyPhone() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "test@example.com")
                        .param("phone", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }

    @Test
    void testPostRegister_UsernameWithSpaces() throws Exception {
        when(userService.userExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "test user")
                        .param("password", "password123")
                        .param("email", "test@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).save(any());
    }
}