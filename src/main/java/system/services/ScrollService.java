package system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import system.models.Scroll;
import system.repositories.ScrollRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrollService {

    private final ScrollRepository scrollRepository;

    public Optional<Scroll> findById(int id) {
        return scrollRepository.findById(id);
    }

    public List<Scroll> findAll() {
        return scrollRepository.findAll();
    }

    public Scroll save(Scroll scroll, MultipartFile contentFile) throws IOException {
        if (scroll.getId() == 0) {
            scroll.setCreatedAt(LocalDateTime.now());
        }
        scroll.setContent(contentFile.getBytes());
        return scrollRepository.save(scroll);
    }

    public void delete(Scroll scroll) {
        scrollRepository.delete(scroll);
    }
}
