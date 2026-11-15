package com.example.imdbopen.director.infrastructure.persistence;

import com.example.imdbopen.director.domain.Realisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DirectorJpaRepository extends JpaRepository<Realisateur, Long> {
    @Query("SELECT d FROM Realisateur d WHERE d.personne.nconst = :idImdb")
    Optional<Realisateur> findByIdImdb(@Param("idImdb") String idImdb);
}
