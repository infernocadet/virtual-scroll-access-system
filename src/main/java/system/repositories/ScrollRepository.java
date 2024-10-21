package system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.models.Scroll;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrollRepository extends JpaRepository<Scroll, Integer> {
    Optional<Scroll> findByNameIgnoreCase(String name);

    /*
    If ur query may return mutliple thigns
     */
    List<Scroll> findByNameContainingIgnoreCase(String name);

}
