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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Scroll scroll = new Scroll();
        scroll.setId(1);
        when(scrollRepository.findById(1)).thenReturn(Optional.of(scroll));

        Optional<Scroll> result = scrollService.findById(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void testFindAll() {
        List<Scroll> scrolls = Arrays.asList(new Scroll(), new Scroll());
        when(scrollRepository.findAll()).thenReturn(scrolls);

        List<Scroll> result = scrollService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testDelete() {
        Scroll scroll = new Scroll();
        scrollService.delete(scroll);
        verify(scrollRepository).delete(scroll);
    }
}