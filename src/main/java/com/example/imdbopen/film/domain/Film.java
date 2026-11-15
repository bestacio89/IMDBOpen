package com.example.imdbopen.film.domain;

import com.example.imdbopen.genre.domain.Genre;
import com.example.imdbopen.country.domain.Pays;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@Table(name = "film", uniqueConstraints = @UniqueConstraint(name = "uk_film_tconst", columnNames = "tconst"))
@Getter
@Setter
@NoArgsConstructor
public class Film {
    private static final Pattern RELEASE_YEAR_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{4})(?:\\D+(\\d{4}))?(?!\\d)");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tconst", nullable = false, length = 20)
    private String tconst;

    @NotBlank
    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @Column(name = "release_year_source", nullable = false, length = 32)
    private String releaseYearSource;

    @Column(name = "release_year_start")
    private Short releaseYearStart;

    @Column(name = "release_year_end")
    private Short releaseYearEnd;

    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal ratingValue;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column(name = "filming_locations", columnDefinition = "TEXT")
    private String filmingLocations;

    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "synopsis", columnDefinition = "LONGTEXT")
    private String synopsis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", foreignKey = @ForeignKey(name = "fk_film_country"))
    private Pays country;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "film_genre",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genresl = new HashSet<>();

    @Transient
    private String genres;

    public String getImdb() { return tconst; }
    public void setImdb(String value) { this.tconst = value; }
    public String getNom() { return title; }
    public void setNom(String value) { this.title = value; }
    public String getAnnee() { return releaseYearSource; }
    public void setAnnee(String value) {
        this.releaseYearSource = value;
        this.releaseYearStart = null;
        this.releaseYearEnd = null;
        if (value == null) return;
        Matcher matcher = RELEASE_YEAR_PATTERN.matcher(value);
        if (matcher.find()) {
            this.releaseYearStart = Short.valueOf(matcher.group(1));
            this.releaseYearEnd = matcher.group(2) == null
                    ? this.releaseYearStart : Short.valueOf(matcher.group(2));
        }
    }
    public String getRating() { return ratingValue == null ? null : ratingValue.toPlainString(); }
    public void setRating(String value) {
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value)) {
            this.ratingValue = null;
            return;
        }
        this.ratingValue = new BigDecimal(value.trim().replace(',', '.'));
    }
    public String getUrl() { return sourceUrl; }
    public void setUrl(String value) { this.sourceUrl = value; }
    public String getLieuTour() { return filmingLocations; }
    public void setLieuTour(String value) { this.filmingLocations = value; }
    public String getLangue() { return language; }
    public void setLangue(String value) {
        this.language = value == null || "None".equalsIgnoreCase(value.trim()) ? null : value.trim();
    }
    public String getResume() { return synopsis; }
    public void setResume(String value) { this.synopsis = value; }
    public Pays getPays() { return country; }
    public void setPays(Pays value) { this.country = value; }
    public String getGenres() {
        if (genres != null) return genres;
        return genresl.stream().map(Genre::getName).collect(Collectors.joining(", "));
    }

}
