package persistence.repository;


import com.example.imdbopen.country.domain.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPaysRepository extends JpaRepository<Pays, Long> {
    Optional<Pays> findByName(String name);
}