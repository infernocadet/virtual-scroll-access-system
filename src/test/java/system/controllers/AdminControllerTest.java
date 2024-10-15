package system.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import system.models.User;
import system.models.Scroll;
import system.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.services.ScrollService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private ScrollService scrollService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllUsers() throws Exception {
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("newUser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUser() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        mockMvc.perform(post("/admin/users/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).deleteById(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithInvalidData() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteNonExistentUser() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userRepository).deleteById(999);

        mockMvc.perform(post("/admin/users/delete/999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).deleteById(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllUsersWithNoUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", Arrays.asList()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithExistingUsername() throws Exception {
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "existingUser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllScrolls() throws Exception {
        List<Scroll> scrolls = Arrays.asList(new Scroll(), new Scroll());
        when(scrollService.findAll()).thenReturn(scrolls);

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"))
                .andExpect(model().attribute("scrolls", scrolls));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllScrollsWithNoScrolls() throws Exception {
        when(scrollService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"))
                .andExpect(model().attribute("scrolls", Arrays.asList()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUnauthorizedAccessToAdminPages() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithEmptyUsername() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithEmptyPassword() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "newUser")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }
}