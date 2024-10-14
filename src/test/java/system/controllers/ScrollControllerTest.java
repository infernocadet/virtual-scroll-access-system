package system.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import system.models.Scroll;
import system.models.User;
import system.services.ScrollService;
import system.services.UserService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    private User testUser;
    private Scroll testScroll;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testScroll = new Scroll();
        testScroll.setId(1);
        testScroll.setName("Test Scroll");
        testScroll.setUser(testUser);

        when(scrollService.findAll()).thenReturn(Arrays.asList(testScroll));
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
    @WithMockUser(username = "testuser")
    void testPostCreateScrollSuccess() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.nameExists("New Scroll")).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("contentFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/scroll/create")
                        .file(file)
                        .param("name", "New Scroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostCreateScrollNameExists() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.nameExists("Existing Scroll")).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("contentFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/scroll/create")
                        .file(file)
                        .param("name", "Existing Scroll")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_create"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDownloadScroll() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        testScroll.setContent("test content".getBytes());
        testScroll.setContentType(MediaType.TEXT_PLAIN_VALUE);
        testScroll.setFileName("test.txt");

        mockMvc.perform(get("/scroll/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""));

        verify(scrollService).save(testScroll);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetEditScroll() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(get("/scroll/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_edit"))
                .andExpect(model().attributeExists("scroll"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostEditScrollSuccess() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.nameExists("Updated Scroll")).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("contentFile", "updated.txt", MediaType.TEXT_PLAIN_VALUE, "updated content".getBytes());

        mockMvc.perform(multipart("/scroll/1/edit")
                        .file(file)
                        .param("name", "Updated Scroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDeleteScroll() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(get("/scroll/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).delete(testScroll);
    }
}