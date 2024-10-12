package system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import system.models.Scroll;
import system.repositories.ScrollRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrollService {

    private final ScrollRepository scrollRepository;

    public boolean nameExists(String name) {
        return scrollRepository.findByNameIgnoreCase(name).isPresent();
    }

    public Optional<Scroll> findById(int id) {
        return scrollRepository.findById(id);
    }

    public List<Scroll> findAll() {
        return scrollRepository.findAll();
    }

    public Scroll save(Scroll scroll) {
        if (scroll.getId() == 0) {
            scroll.setCreatedAt(LocalDateTime.now());
        }
        return scrollRepository.save(scroll);
    }

    public void delete(Scroll scroll) {
        scrollRepository.delete(scroll);
    }
}
