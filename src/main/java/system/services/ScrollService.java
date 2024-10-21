package system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import system.models.Scroll;
import system.repositories.ScrollRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrollService {

    private final ScrollRepository scrollRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - hh:mm a");

    public boolean nameExists(String name) {
        return scrollRepository.findByNameIgnoreCase(name).isPresent();
    }

    public Optional<Scroll> findById(int id) {
        return scrollRepository.findById(id).map(this::formatScrollDates);
    }

    public List<Scroll> findAll() {
        List<Scroll> scrolls = scrollRepository.findAll();
        scrolls.forEach(this::formatScrollDates);
        return scrolls;
    }

    public List<Scroll> findByName(String name){
        return scrollRepository.findByNameContainingIgnoreCase(name);
    }

    public Scroll save(Scroll scroll) {
        if (scroll.getId() == 0) {
            // for new scroll
            scroll.setCreatedAt(LocalDateTime.now());
            scroll.setUpdatedAt(LocalDateTime.now());
        } else {
            // if scroll exists but being updated
            scroll.setUpdatedAt(LocalDateTime.now());
        }
        return scrollRepository.save(scroll);
    }

    private Scroll formatScrollDates(Scroll scroll){
        if (scroll.getCreatedAt() != null){
            scroll.setFormattedCreatedAt(scroll.getCreatedAt().format(formatter));
        }
        if (scroll.getUpdatedAt() != null){
            scroll.setFormattedUpdatedAt(scroll.getUpdatedAt().format(formatter));
        }
        return scroll;
    }

    public void delete(Scroll scroll) {
        scrollRepository.delete(scroll);
    }
}
