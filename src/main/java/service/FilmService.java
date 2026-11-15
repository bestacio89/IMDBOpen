package service;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.country.domain.Pays;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.genre.application.port.out.GenreStore;
import com.example.imdbopen.genre.domain.Genre;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import persistence.repository.IFilmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import web.model.dto.FilmDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FilmService {
    private final IFilmRepository filmRepository;
    private final GenreStore genreStore;
    private final CountryStore countryStore;

    public FilmService(IFilmRepository filmRepository, GenreStore genreStore, CountryStore countryStore) {
        this.filmRepository = filmRepository;
        this.genreStore = genreStore;
        this.countryStore = countryStore;
    }

    public List<FilmDTO> findFilmsWithFiltersAndSorting(String nom, String annee, String rating,
                                                        String paysName, String genreName, String sortBy) {
        List<Film> films = filmRepository.findAll();
        if (StringUtils.hasText(nom)) {
            films = films.stream().filter(f -> f.getNom() != null
                    && f.getNom().toLowerCase().contains(nom.toLowerCase())).collect(Collectors.toList());
        }
        if (StringUtils.hasText(annee)) {
            films = films.stream().filter(f -> f.getAnnee() != null && f.getAnnee().contains(annee))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(rating)) {
            films = films.stream().filter(f -> f.getRating() != null && f.getRating().contains(rating))
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(paysName)) {
            films = films.stream().filter(f -> f.getPays() != null
                    && f.getPays().getName().equalsIgnoreCase(paysName)).collect(Collectors.toList());
        }
        if (StringUtils.hasText(genreName)) {
            films = films.stream().filter(f -> Arrays.stream(f.getGenres().split(",\\\\s*"))
                    .anyMatch(genre -> genre.equalsIgnoreCase(genreName))).collect(Collectors.toList());
        }
        if (StringUtils.hasText(sortBy)) {
            boolean ascending = !sortBy.startsWith("-");
            String sortField = ascending ? sortBy : sortBy.substring(1);
            films = films.stream().sorted((left, right) ->
                    compareFilmsByField(left, right, sortField, ascending)).collect(Collectors.toList());
        }
        if (films.isEmpty()) {
            throw new EntityNotFoundException("No films found matching the criteria");
        }
        return films.stream().map(FilmDTO::fromEntity).collect(Collectors.toList());
    }

    private int compareFilmsByField(Film left, Film right, String field, boolean ascending) {
        int comparison = switch (field) {
            case "nom" -> safeCompare(left.getNom(), right.getNom());
            case "annee" -> safeCompare(left.getAnnee(), right.getAnnee());
            case "rating" -> safeCompare(left.getRating(), right.getRating());
            default -> throw new IllegalArgumentException("Unsupported sorting field: " + field);
        };
        return ascending ? comparison : -comparison;
    }

    private int safeCompare(String left, String right) {
        if (left == null) {
            return right == null ? 0 : -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareToIgnoreCase(right);
    }

    public Optional<FilmDTO> findFilmByImdb(String imdb) {
        return filmRepository.findByImdb(imdb).map(FilmDTO::fromEntity);
    }

    public List<FilmDTO> findFilmsByName(String nom) {
        List<Film> films = filmRepository.findByNomContainingIgnoreCase(nom);
        if (films.isEmpty()) {
            throw new EntityNotFoundException("No films found with name containing: " + nom);
        }
        return films.stream().map(FilmDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public FilmDTO createFilm(FilmDTO filmDTO) {
        if (filmDTO == null) {
            throw new InvalidDataException("Film data cannot be null");
        }
        Film film = filmDTO.toEntity();
        film.setPays(resolveCountry(filmDTO.getPays()));
        synchronizeGenres(film, filmDTO.getGenres());
        return FilmDTO.fromEntity(filmRepository.save(film));
    }

    @Transactional
    public FilmDTO updateFilm(Long id, FilmDTO filmDTO) {
        if (filmDTO == null) {
            throw new InvalidDataException("Film data cannot be null");
        }
        Film existing = filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Film not found with ID: " + id));
        existing.setNom(filmDTO.getNom());
        existing.setAnnee(filmDTO.getAnnee());
        existing.setRating(filmDTO.getRating());
        existing.setUrl(filmDTO.getUrl());
        existing.setLieuTour(filmDTO.getLieuTour());
        existing.setLangue(filmDTO.getLangue());
        existing.setResume(filmDTO.getResume());
        existing.setPays(resolveCountry(filmDTO.getPays()));
        synchronizeGenres(existing, filmDTO.getGenres());
        return FilmDTO.fromEntity(filmRepository.save(existing));
    }

    @Transactional
    public void deleteFilm(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Film not found with ID: " + id));
        filmRepository.delete(film);
    }

    private Pays resolveCountry(Pays requested) {
        if (requested == null) {
            return null;
        }
        if (requested.getId() != null) {
            return countryStore.findById(requested.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Country not found with ID: " + requested.getId()));
        }
        if (StringUtils.hasText(requested.getName())) {
            return findOrCreatePays(requested.getName());
        }
        throw new InvalidDataException("Country must have an ID or a name");
    }

    private void synchronizeGenres(Film film, String genreNames) {
        if (!StringUtils.hasText(genreNames)) {
            film.setGenresl(new java.util.HashSet<>());
            film.setGenres(null);
            return;
        }
        Set<Genre> genres = Arrays.stream(genreNames.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::findOrCreateGenre)
                .collect(Collectors.toSet());
        film.setGenresl(genres);
        film.setGenres(null);
    }

    @Transactional
    public Genre findOrCreateGenre(String genreName) {
        return genreStore.findByName(genreName)
                .orElseGet(() -> genreStore.save(new Genre(null, genreName)));
    }

    @Transactional
    public Pays findOrCreatePays(String paysName) {
        return countryStore.findByName(paysName)
                .orElseGet(() -> countryStore.save(new Pays(null, paysName)));
    }

    @Transactional
    public void saveAll(List<Film> films) {
        filmRepository.saveAll(films);
    }

    @Transactional
    public void save(Film film) {
        filmRepository.saveAndFlush(film);
    }
}
