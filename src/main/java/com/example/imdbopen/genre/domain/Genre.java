package com.example.imdbopen.genre.domain;

import com.example.imdbopen.film.domain.Film;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "genre", uniqueConstraints = @UniqueConstraint(name = "uk_genre_name", columnNames = "normalized_name"))
@Getter
@Setter
@NoArgsConstructor
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @ManyToMany(mappedBy = "genresl", fetch = FetchType.LAZY)
    private Set<Film> films = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void normalizeName() {
        if (name != null) {
            normalizedName = name.trim().toLowerCase(Locale.ROOT);
        }
    }

    public Genre(Long id, String name) {
        this.id = id;
        this.name = name;
        this.normalizedName = name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    public Genre orElseGet(Object ignored) { return this; }
}
