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
        testScroll.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testNameExists() {
        when(scrollRepository.findByNameIgnoreCase("Test Scroll")).thenReturn(Optional.of(testScroll));
        when(scrollRepository.findByNameIgnoreCase("Non-existent Scroll")).thenReturn(Optional.empty());

        assertTrue(scrollService.nameExists("Test Scroll"));
        assertFalse(scrollService.nameExists("Non-existent Scroll"));
    }

    @Test
    void testFindByName() {
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByNameContainingIgnoreCase("Test")).thenReturn(scrolls);

        List<Scroll> result = scrollService.findByName("Test");

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testFindByUserID() {
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByUserId(1)).thenReturn(scrolls);

        List<Scroll> result = scrollService.findByUserID(1);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByCreatedAtAfter(date)).thenReturn(scrolls);

        List<Scroll> result = scrollService.findByCreatedAtAfter(date);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByCreatedAtBetween(startDate, endDate)).thenReturn(scrolls);

        List<Scroll> result = scrollService.findByCreatedAtBetween(startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testSearchScrollsWithUserId() {
        List<Scroll> userScrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByUserId(1)).thenReturn(userScrolls);

        List<Scroll> result = scrollService.searchScrolls(1, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testSearchScrollsWithName() {
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByNameContainingIgnoreCase("Test")).thenReturn(scrolls);

        List<Scroll> result = scrollService.searchScrolls(null, null, "Test", null, null);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testSearchScrollsWithNonExistentScrollId() {
        when(scrollRepository.findById(999)).thenReturn(Optional.empty());

        List<Scroll> result = scrollService.searchScrolls(null, 999, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchScrollsWithDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        List<Scroll> scrolls = Arrays.asList(testScroll);
        when(scrollRepository.findByCreatedAtBetween(startDate, endDate)).thenReturn(scrolls);

        List<Scroll> result = scrollService.searchScrolls(null, null, null, startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("Test Scroll", result.get(0).getName());
    }

    @Test
    void testSearchScrollsWithNoParams() {
        List<Scroll> result = scrollService.searchScrolls(null, null, null, null, null);

        assertTrue(result.isEmpty());
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

    @Test
    void testFormatScrollDates() {
        when(scrollRepository.findById(1)).thenReturn(Optional.of(testScroll));

        Optional<Scroll> result = scrollService.findById(1);

        assertTrue(result.isPresent());
        Scroll scroll = result.get();
        assertNotNull(scroll.getFormattedCreatedAt());
        assertNotNull(scroll.getFormattedUpdatedAt());
    }

    @Test
    void testSaveExistingScrollUpdateTime() {
        LocalDateTime originalUpdatedAt = testScroll.getUpdatedAt();
        when(scrollRepository.save(any(Scroll.class))).thenReturn(testScroll);

        Scroll result = scrollService.save(testScroll);

        assertNotNull(result);
        assertTrue(result.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    void testFindAllWithEmptyList() {
        when(scrollRepository.findAll()).thenReturn(Arrays.asList());

        List<Scroll> result = scrollService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteNonExistentScroll() {
        Scroll nonExistentScroll = new Scroll();
        nonExistentScroll.setId(999);

        scrollService.delete(nonExistentScroll);

        verify(scrollRepository).delete(nonExistentScroll);
    }
}