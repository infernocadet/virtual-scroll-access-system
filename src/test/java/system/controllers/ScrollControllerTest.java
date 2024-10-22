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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testPostCreateScrollEmptyName() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        MockMultipartFile file = new MockMultipartFile("contentFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        mockMvc.perform(multipart("/scroll/create")
                        .file(file)
                        .param("name", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_create"))
                .andExpect(model().attributeExists("error"));

        verify(scrollService, never()).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostCreateScrollEmptyFile() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        MockMultipartFile file = new MockMultipartFile("contentFile", "", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(multipart("/scroll/create")
                        .file(file)
                        .param("name", "New Scroll")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_create"))
                .andExpect(model().attributeExists("error"));

        verify(scrollService, never()).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostEditScrollNameExists() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.nameExists("Existing Scroll")).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("contentFile", "", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(multipart("/scroll/1/edit")
                        .file(file)
                        .param("name", "Existing Scroll")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_edit"))
                .andExpect(model().attributeExists("error"));

        verify(scrollService, never()).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "otheruser")
    void testGetEditScrollUnauthorized() throws Exception {
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("otheruser")).thenReturn(otherUser);

        mockMvc.perform(get("/scroll/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDownloadScroll() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setContent("test content".getBytes());
        scroll.setContentType(MediaType.TEXT_PLAIN_VALUE);
        scroll.setFileName("test.txt");
        scroll.setUser(testUser);

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));
        when(scrollService.save(any(Scroll.class))).thenReturn(scroll);

        mockMvc.perform(post("/scroll/1/download")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(content().bytes("test content".getBytes()));
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
        Scroll existingScroll = new Scroll();
        existingScroll.setId(1);
        existingScroll.setName("Old Name");
        existingScroll.setUser(testUser);
        existingScroll.setContent("old content".getBytes());
        existingScroll.setContentType(MediaType.TEXT_PLAIN_VALUE);
        existingScroll.setFileName("old.txt");
        existingScroll.setPassword("oldPassword");

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.findById(1)).thenReturn(Optional.of(existingScroll));
        when(scrollService.nameExists("Updated Scroll")).thenReturn(false);
        when(scrollService.save(any(Scroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "contentFile",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes()
        );

        // Perform the request
        mockMvc.perform(multipart("/scroll/1/edit")
                        .file(file)
                        .param("name", "Updated Scroll")
                        .param("password", "oldPassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verify the service call
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

    @Test
    @WithMockUser(username = "testuser")
    void testSearchScroll() throws Exception {
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        Scroll searchResult = new Scroll();
        searchResult.setId(2);
        searchResult.setName("Search Result");
        searchResult.setUser(testUser);

        when(scrollService.searchScrolls(any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(searchResult));

        mockMvc.perform(get("/scroll/search")
                        .param("uploaderId", "1")
                        .param("name", "Search")
                        .param("startDate", "2023-01-01T00:00")
                        .param("endDate", "2023-12-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("scrolls"));

        verify(scrollService).searchScrolls(eq(1), eq(null), eq("Search"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSearchScrollNoResults() throws Exception {
        when(scrollService.searchScrolls(any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/scroll/search")
                        .param("name", "NonExistent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetEditScrollNonExistent() throws Exception {
        when(scrollService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/scroll/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostEditScrollEmptyName() throws Exception {
        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(multipart("/scroll/1/edit")
                        .param("name", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_edit"))
                .andExpect(model().attributeExists("error"));

        verify(scrollService, never()).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDeleteScrollNonExistent() throws Exception {
        when(scrollService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/scroll/999/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService, never()).delete(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "otheruser")
    void testGetDeleteScrollUnauthorized() throws Exception {
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(scrollService.findById(1)).thenReturn(Optional.of(testScroll));
        when(userService.findByUsername("otheruser")).thenReturn(otherUser);

        mockMvc.perform(get("/scroll/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService, never()).delete(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDownloadScrollNonExistent() throws Exception {
        when(scrollService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/scroll/999/download")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetDownloadScrollIncrementsDownloadCount() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setContent("test content".getBytes());
        scroll.setContentType(MediaType.TEXT_PLAIN_VALUE);
        scroll.setFileName("test.txt");
        scroll.setUser(testUser);
        scroll.setDownloads(5);

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));
        when(scrollService.save(any(Scroll.class))).thenAnswer(invocation -> {
            Scroll savedScroll = invocation.getArgument(0);
            assertEquals(6, savedScroll.getDownloads());
            return savedScroll;
        });

        mockMvc.perform(post("/scroll/1/download")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(scrollService).save(argThat(s -> s.getDownloads() == 6));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDownloadScrollWithCorrectPassword() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setContent("test content".getBytes());
        scroll.setContentType(MediaType.TEXT_PLAIN_VALUE);
        scroll.setFileName("test.txt");
        scroll.setUser(testUser);
        scroll.setPassword("correctPassword");

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));

        mockMvc.perform(post("/scroll/1/download")
                        .param("password", "correctPassword")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE));

        verify(scrollService).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDownloadScrollWithWrongPassword() throws Exception {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        scroll.setPassword("correctPassword");
        scroll.setUser(testUser);

        when(scrollService.findById(1)).thenReturn(Optional.of(scroll));
        when(scrollService.findAll()).thenReturn(Arrays.asList(scroll));

        mockMvc.perform(post("/scroll/1/download")
                        .param("password", "wrongPassword")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("error", "Wrong password"));

        verify(scrollService, never()).save(any(Scroll.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostEditScrollWithPasswordChange() throws Exception {
        Scroll existingScroll = new Scroll();
        existingScroll.setId(1);
        existingScroll.setName("Old Name");
        existingScroll.setUser(testUser);
        existingScroll.setPassword("oldPassword");

        when(scrollService.findById(1)).thenReturn(Optional.of(existingScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(multipart("/scroll/1/edit")
                        .file(new MockMultipartFile("contentFile", new byte[0]))
                        .param("name", "Old Name")
                        .param("password", "newPassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(scrollService).save(argThat(s -> s.getPassword().equals("newPassword")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testPostEditScrollDuplicateName() throws Exception {
        Scroll existingScroll = new Scroll();
        existingScroll.setId(1);
        existingScroll.setUser(testUser);
        existingScroll.setName("Old Name");

        when(scrollService.findById(1)).thenReturn(Optional.of(existingScroll));
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(scrollService.nameExists("New Name")).thenReturn(true);

        mockMvc.perform(multipart("/scroll/1/edit")
                        .file(new MockMultipartFile("contentFile", new byte[0]))
                        .param("name", "New Name")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("scroll_edit"))
                .andExpect(model().attributeExists("error"));

        verify(scrollService, never()).save(any());
    }
}