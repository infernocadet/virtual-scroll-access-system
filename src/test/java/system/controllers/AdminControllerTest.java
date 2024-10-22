package system.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import system.models.Scroll;
import system.models.User;
import system.repositories.ScrollRepository;
import system.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import system.services.ScrollService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasEntry;
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

    @MockBean
    private ScrollRepository scrollRepository;

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
    void testAddUserWithLongUsername() throws Exception {
        String longUsername = "a".repeat(256);
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", longUsername)
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithSpecialCharactersInUsername() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "user@name")
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
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", Collections.emptyList()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllUsersWithScrolls() throws Exception {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        List<Scroll> scrolls = Arrays.asList(new Scroll(), new Scroll());
        user1.setScrolls(scrolls);

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setScrolls(null);

        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_users"))
                .andExpect(model().attributeExists("users", "userScrollCounts"))
                .andExpect(model().attribute("userScrollCounts", hasEntry(user1, 2)))
                .andExpect(model().attribute("userScrollCounts", hasEntry(user2, 0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithNullUsername() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithNullPassword() throws Exception {
        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMakeAdmin() throws Exception {
        User user = new User();
        user.setId(1);
        user.setAdmin(false);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/admin/users/makeAdmin/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).save(argThat(savedUser -> savedUser.isAdmin()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMakeAdminInvalidId() throws Exception {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/users/makeAdmin/999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdmin() throws Exception {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/admin/users/demoteAdmin/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).save(argThat(savedUser -> !savedUser.isAdmin()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminInvalidId() throws Exception {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/users/demoteAdmin/999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMakeAdminWithException() throws Exception {
        when(userRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/admin/users/makeAdmin/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDemoteAdminWithException() throws Exception {
        when(userRepository.findById(1)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/admin/users/demoteAdmin/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
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
    void testViewAllScrollsWithNoScrolls() throws Exception {
        when(scrollService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"))
                .andExpect(model().attribute("scrolls", Collections.emptyList()));
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddUserWithSuccessfulSave() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        mockMvc.perform(post("/admin/users/add")
                        .with(csrf())
                        .param("username", "newUser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(userRepository).deleteById(1);

        mockMvc.perform(post("/admin/users/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userRepository).deleteById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllScrollsNoSort() throws Exception {
        when(scrollRepository.findAll()).thenReturn(Arrays.asList(new Scroll(), new Scroll()));

        mockMvc.perform(get("/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"));

        verify(scrollRepository).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllScrollsSortAsc() throws Exception {
        when(scrollRepository.findAllByOrderByDownloadsAsc()).thenReturn(Arrays.asList(new Scroll(), new Scroll()));

        mockMvc.perform(get("/admin/statistics").param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"));

        verify(scrollRepository).findAllByOrderByDownloadsAsc();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewAllScrollsSortDesc() throws Exception {
        when(scrollRepository.findAllByOrderByDownloadsDesc()).thenReturn(Arrays.asList(new Scroll(), new Scroll()));

        mockMvc.perform(get("/admin/statistics").param("sort", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_scrolls"))
                .andExpect(model().attributeExists("scrolls"));

        verify(scrollRepository).findAllByOrderByDownloadsDesc();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testIncreaseDownloads() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setDownloads(5);

        when(scrollRepository.findById(1)).thenReturn(Optional.of(scroll));

        mockMvc.perform(post("/admin/scrolls/increase/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/statistics?sort=asc"));

        verify(scrollRepository).save(argThat(s -> s.getDownloads() == 6));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDecreaseDownloads() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setDownloads(5);

        when(scrollRepository.findById(1)).thenReturn(Optional.of(scroll));

        mockMvc.perform(post("/admin/scrolls/decrease/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/statistics?sort=asc"));

        verify(scrollRepository).save(argThat(s -> s.getDownloads() == 4));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDecreaseDownloadsAtZero() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setDownloads(0);

        when(scrollRepository.findById(1)).thenReturn(Optional.of(scroll));

        mockMvc.perform(post("/admin/scrolls/decrease/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/statistics?sort=asc"));

        verify(scrollRepository).save(argThat(s -> s.getDownloads() == 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchUsers() throws Exception {
        mockMvc.perform(get("/admin/users/search")
                        .param("username", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/view_users"))
                .andExpect(model().attributeExists("users", "newUser", "userScrollCounts"));
    }
}