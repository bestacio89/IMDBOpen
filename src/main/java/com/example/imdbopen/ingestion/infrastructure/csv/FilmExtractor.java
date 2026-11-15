package com.example.imdbopen.ingestion.infrastructure.csv;

import com.example.imdbopen.film.application.FilmService;
import com.example.imdbopen.film.contracts.CountryReference;
import com.example.imdbopen.film.contracts.FilmDTO;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Component
public class FilmExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilmExtractor.class);
    private final FilmService filmService;

    public FilmExtractor(FilmService filmService) {
        this.filmService = filmService;
    }

    @Transactional
    public void extractAndSaveFilmsFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            reader.readNext();
            String[] line;
            long rowNumber = 1;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                if (line.length < 10) {
                    LOGGER.warn("Skipping film CSV row {}: expected 10 columns but found {}", rowNumber, line.length);
                    continue;
                }
                try {
                    String imdbId = CsvValueNormalizer.normalize(line[0]);
                    if (imdbId == null) {
                        LOGGER.warn("Skipping film CSV row {} because the IMDb identifier is missing", rowNumber);
                        continue;
                    }
                    Optional<FilmDTO> existingFilm = filmService.findFilmByImdb(imdbId);
                    if (existingFilm.isPresent()) {
                        continue;
                    }

                    String summary = CsvValueNormalizer.normalize(line[8]);
                    if (summary != null && summary.length() > 10_000) {
                        LOGGER.warn("Truncating summary for film {} to 10,000 characters", imdbId);
                        summary = summary.substring(0, 10_000);
                    }

                    FilmDTO film = new FilmDTO();
                    film.setImdb(imdbId);
                    film.setNom(CsvValueNormalizer.normalize(line[1]));
                    film.setAnnee(CsvValueNormalizer.normalize(line[2]));
                    film.setRating(CsvValueNormalizer.normalize(line[3]));
                    film.setUrl(CsvValueNormalizer.normalize(line[4]));
                    film.setLieuTour(CsvValueNormalizer.normalize(line[5]));
                    film.setGenres(CsvValueNormalizer.normalize(line[6]));
                    film.setLangue(CsvValueNormalizer.normalize(line[7]));
                    film.setResume(summary);

                    String countryName = CsvValueNormalizer.normalize(line[9]);
                    if (countryName != null) {
                        CountryReference country = new CountryReference();
                        country.setName(countryName);
                        film.setPays(country);
                    }
                    filmService.createFilm(film);
                } catch (RuntimeException exception) {
                    LOGGER.warn("Failed to import film from CSV row {}", rowNumber, exception);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Unable to read film CSV file: " + filePath, exception);
        }
    }
}
