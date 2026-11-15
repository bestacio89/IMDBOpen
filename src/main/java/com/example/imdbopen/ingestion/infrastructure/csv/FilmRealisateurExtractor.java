package com.example.imdbopen.ingestion.infrastructure.csv;

import com.example.imdbopen.film.application.FilmDirectorService;
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

@Component
public class FilmRealisateurExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilmRealisateurExtractor.class);
    private final FilmDirectorService filmDirectorService;

    public FilmRealisateurExtractor(FilmDirectorService filmDirectorService) {
        this.filmDirectorService = filmDirectorService;
    }

    @Transactional
    public void extractAndSaveFilmRealisateurFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            String[] line;
            long rowNumber = 0;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                if (line.length < 2) {
                    LOGGER.warn("Skipping film/director CSV row {}: expected 2 columns but found {}",
                            rowNumber, line.length);
                    continue;
                }
                try {
                    String filmId = CsvValueNormalizer.normalize(line[0]);
                    String directorId = CsvValueNormalizer.normalize(line[1]);
                    if (filmId != null && directorId != null) {
                        filmDirectorService.assignDirectorToFilm(filmId, directorId);
                    }
                } catch (RuntimeException exception) {
                    LOGGER.warn("Failed to import film/director association from CSV row {}", rowNumber, exception);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Unable to read film/director CSV file: " + filePath, exception);
        }
    }
}
