package system.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import system.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
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
                        .param("phone", "123456")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Phone number must be 10 digits"));

        verify(userService, never()).save(any());
    }
}