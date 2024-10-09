package system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import system.models.Scroll;

@Repository
public interface ScrollRepository  extends JpaRepository<Scroll, Integer> { }
