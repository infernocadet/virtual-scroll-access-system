package system.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import system.models.Scroll;
import system.repositories.ScrollRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScrollServiceTest {

    @Mock
    private ScrollRepository scrollRepository;

    @InjectMocks
    private ScrollService scrollService;

    private Scroll testScroll;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testScroll = new Scroll();
        testScroll.setId(1);
        testScroll.setName("Test Scroll");
        testScroll.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testNameExists() {
        when(scrollRepository.findByNameIgnoreCase("Test Scroll")).thenReturn(Optional.of(testScroll));
        when(scrollRepository.findByNameIgnoreCase("Non-existent Scroll")).thenReturn(Optional.empty());

        assertTrue(scrollService.nameExists("Test Scroll"));
        assertFalse(scrollService.nameExists("Non-existent Scroll"));
    }

    @Test
    void testFindById() {
        when(scrollRepository.findById(1)).thenReturn(Optional.of(testScroll));
        when(scrollRepository.findById(2)).thenReturn(Optional.empty());

        Optional<Scroll> result = scrollService.findById(1);
        assertTrue(result.isPresent());
        assertEquals("Test Scroll", result.get().getName());

        Optional<Scroll> nonExistentResult = scrollService.findById(2);
        assertTrue(nonExistentResult.isEmpty());
    }

    @Test
    void testFindAll() {
        Scroll scroll2 = new Scroll();
        scroll2.setId(2);
        scroll2.setName("Another Scroll");

        List<Scroll> scrolls = Arrays.asList(testScroll, scroll2);
        when(scrollRepository.findAll()).thenReturn(scrolls);

        List<Scroll> result = scrollService.findAll();
        assertEquals(2, result.size());
        assertTrue(result.contains(testScroll));
        assertTrue(result.contains(scroll2));
    }

    @Test
    void testSaveNewScroll() {
        Scroll newScroll = new Scroll();
        newScroll.setName("New Scroll");

        when(scrollRepository.save(any(Scroll.class))).thenAnswer(invocation -> {
            Scroll savedScroll = invocation.getArgument(0);
            savedScroll.setId(3);
            return savedScroll;
        });

        Scroll result = scrollService.save(newScroll);

        assertNotNull(result.getId());
        assertEquals("New Scroll", result.getName());
        assertNotNull(result.getCreatedAt());
        verify(scrollRepository).save(newScroll);
    }

    @Test
    void testSaveExistingScroll() {
        when(scrollRepository.save(any(Scroll.class))).thenReturn(testScroll);

        Scroll result = scrollService.save(testScroll);

        assertEquals(1, result.getId());
        assertEquals("Test Scroll", result.getName());
        verify(scrollRepository).save(testScroll);
    }

    @Test
    void testDelete() {
        scrollService.delete(testScroll);
        verify(scrollRepository).delete(testScroll);
    }
}