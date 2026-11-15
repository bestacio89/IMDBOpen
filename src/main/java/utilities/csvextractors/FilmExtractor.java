package utilities.csvextractors;

import com.example.imdbopen.country.application.PaysService;
import com.example.imdbopen.country.domain.Pays;
import com.example.imdbopen.film.domain.Film;
import com.example.imdbopen.genre.application.GenreService;
import com.example.imdbopen.genre.domain.Genre;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.imdbopen.film.application.FilmService;
import web.model.dto.FilmDTO;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class FilmExtractor {
    private final FilmService filmService;
    private final PaysService paysService;
    private final GenreService genreService;

    public FilmExtractor(FilmService filmService, PaysService paysService, GenreService genreService) {
        this.filmService = filmService;
        this.paysService = paysService;
        this.genreService = genreService;
    }

    @Transactional
    public void extractAndSaveFilmsFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            reader.readNext();
            String[] line;

            while ((line = reader.readNext()) != null) {
                try {
                    String imdb = line[0].isEmpty() ? "N/A" : line[0].trim();
                    String nom = line[1].isEmpty() ? "N/A" : line[1].trim();
                    String annee = line[2].isEmpty() ? "N/A" : line[2].trim();
                    String rating = line[3].isEmpty() ? "N/A" : line[3].trim();
                    String url = line[4].isEmpty() ? "N/A" : line[4].trim();
                    String lieuTour = line[5].isEmpty() ? "N/A" : line[5].trim();
                    String genre = line[6].isEmpty() ? "N/A" : line[6].trim();
                    String langue = line[7].isEmpty() ? "N/A" : line[7].trim();
                    String resume = line[8].isEmpty() ? "N/A" : line[8].trim();
                    String paysName = line[9].isEmpty() ? "N/A" : line[9].trim();

                    if (resume.length() > 10000) {
                        System.err.println("Resume too long for film: " + nom + ". Truncating to 10,000 characters.");
                        resume = resume.substring(0, 10000);
                    }

                    Optional<FilmDTO> existingFilm = filmService.findFilmByImdb(imdb);
                    if (existingFilm.isEmpty()) {
                        Pays pays = paysService.findOrCreatePays(paysName);
                        Set<Genre> genres = new HashSet<>();
                        for (String genreName : genre.split(",")) {
                            genres.add(genreService.findOrCreateGenre(genreName.trim()));
                        }

                        Film film = new Film();
                        film.setImdb(imdb);
                        film.setNom(nom);
                        film.setAnnee(annee);
                        film.setRating(rating);
                        film.setUrl(url);
                        film.setLieuTour(lieuTour);
                        film.setLangue(langue);
                        film.setResume(resume);
                        film.setPays(pays);
                        film.setGenres(genre);
                        filmService.save(film);
                    } else {
                        System.out.println("Film with IMDb ID " + imdb + " already exists. Skipping...");
                    }
                } catch (Exception exception) {
                    System.err.println("Error processing line for film: "
                            + (line.length > 1 ? line[1] : "Unknown") + " - " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        } catch (IOException exception) {
            System.err.println("Error reading the CSV file: " + exception.getMessage());
            exception.printStackTrace();
        } catch (CsvValidationException exception) {
            System.err.println("CSV validation error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
