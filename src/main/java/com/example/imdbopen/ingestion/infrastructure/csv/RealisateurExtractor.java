package com.example.imdbopen.ingestion.infrastructure.csv;

import com.example.imdbopen.director.application.RealisateurService;
import com.example.imdbopen.director.contracts.RealisateurDTO;
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
public class RealisateurExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealisateurExtractor.class);
    private final RealisateurService directorService;

    public RealisateurExtractor(RealisateurService directorService) {
        this.directorService = directorService;
    }

    @Transactional
    public void extractRealisateursFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            reader.readNext();
            String[] line;
            long rowNumber = 1;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                if (line.length < 5) {
                    LOGGER.warn("Skipping director CSV row {}: expected at least 5 columns but found {}",
                            rowNumber, line.length);
                    continue;
                }
                try {
                    String imdbId = CsvValueNormalizer.normalize(line[0]);
                    if (imdbId == null || directorService.findByIdImdb(imdbId).isPresent()) {
                        continue;
                    }
                    RealisateurDTO director = new RealisateurDTO();
                    director.setIdImdb(imdbId);
                    director.setIdentite(CsvValueNormalizer.normalize(line[1]));
                    director.setDateNaissance(CsvValueNormalizer.normalize(line[2]));
                    director.setLieuNaissance(CsvValueNormalizer.normalize(line[3]));
                    director.setUrl(CsvValueNormalizer.normalize(line[4]));
                    directorService.save(director);
                } catch (RuntimeException exception) {
                    LOGGER.warn("Failed to import director from CSV row {}", rowNumber, exception);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Unable to read director CSV file: " + filePath, exception);
        }
    }
}
