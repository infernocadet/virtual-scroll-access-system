package system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import system.models.Scroll;
import system.repositories.ScrollRepository;

import java.time.LocalDate;
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

    public List<Scroll> findByUserID(int userId){
        return scrollRepository.findByUserId(userId);
    }

    public List<Scroll> findByCreatedAtAfter(LocalDateTime date){
        return scrollRepository.findByCreatedAtAfter(date);
    }

    public List<Scroll> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate){
        return scrollRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<Scroll> searchScrolls(Integer userId, Integer scrollId, String name, LocalDateTime startDate, LocalDateTime endDate) {

        if (scrollId != null){
            return scrollRepository.findById(scrollId).stream().toList();
        }

        List<Scroll> result = new ArrayList<>();

        if (userId != null) {
            result = scrollRepository.findByUserId(userId);
        } else if (name != null && !name.isEmpty()) {
            result = scrollRepository.findByNameContainingIgnoreCase(name);
        } else if (startDate != null && endDate != null) {
            result = scrollRepository.findByCreatedAtBetween(startDate, endDate);
        }

        return result;
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
