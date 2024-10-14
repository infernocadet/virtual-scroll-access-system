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
import system.services.ScrollService;
import system.services.UserService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScrollService scrollService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");

        Scroll scroll1 = new Scroll();
        scroll1.setId(1);
        scroll1.setName("Scroll 1");
        scroll1.setUser(user1);

        Scroll scroll2 = new Scroll();
        scroll2.setId(2);
        scroll2.setName("Scroll 2");
        scroll2.setUser(user2);

        when(scrollService.findAll()).thenReturn(Arrays.asList(scroll1, scroll2));
    }

    @Test
    @WithMockUser
    void testGetIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("scrolls"));
    }

    @Test
    @WithMockUser
    void testGetCreateScroll() throws Exception {
        mockMvc.perform(get("/scroll/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_create"))
                .andExpect(model().attributeExists("scroll"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testPostCreateScrollSuccess() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        when(userService.findByUsername("testUser")).thenReturn(user);
        when(scrollService.nameExists("TestScroll")).thenReturn(false);

        mockMvc.perform(multipart("/scroll/create")
                        .file("contentFile", "test content".getBytes())
                        .param("name", "TestScroll"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testGetEditScroll() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setName("Test Scroll");
        scroll.setUser(user);

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));
        when(userService.findByUsername("testUser")).thenReturn(user);

        mockMvc.perform(get("/scroll/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_edit"))
                .andExpect(model().attributeExists("scroll"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testPostEditScrollSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        Scroll oldScroll = new Scroll();
        oldScroll.setId(1);
        oldScroll.setName("Old Scroll");
        oldScroll.setUser(user);

        when(scrollService.findById(1)).thenReturn(Optional.of(oldScroll));
        when(userService.findByUsername("testUser")).thenReturn(user);
        when(scrollService.nameExists("New Scroll")).thenReturn(false);

        mockMvc.perform(multipart("/scroll/1/edit")
                        .file("contentFile", "new content".getBytes())
                        .param("name", "New Scroll"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testGetDeleteScroll() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setUser(user);

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));
        when(userService.findByUsername("testUser")).thenReturn(user);

        mockMvc.perform(get("/scroll/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).delete(scroll);
    }
}