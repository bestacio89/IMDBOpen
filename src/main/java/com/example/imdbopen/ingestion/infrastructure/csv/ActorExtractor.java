package com.example.imdbopen.ingestion.infrastructure.csv;

import com.example.imdbopen.actor.application.ActeurService;
import com.example.imdbopen.actor.contracts.ActeurDTO;
import com.example.imdbopen.person.contracts.PersonneDTO;
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
public class ActorExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActorExtractor.class);
    private final ActeurService actorService;

    public ActorExtractor(ActeurService actorService) {
        this.actorService = actorService;
    }

    @Transactional
    public void extractActorsFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            reader.readNext();
            String[] line;
            long rowNumber = 1;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                if (line.length < 6) {
                    LOGGER.warn("Skipping actor CSV row {}: expected 6 columns but found {}", rowNumber, line.length);
                    continue;
                }
                try {
                    String imdbId = CsvValueNormalizer.normalize(line[0]);
                    if (imdbId == null) {
                        LOGGER.warn("Skipping actor CSV row {} because the IMDb identifier is missing", rowNumber);
                        continue;
                    }
                    if (actorService.findByImdb(imdbId).isPresent()) {
                        continue;
                    }

                    PersonneDTO person = new PersonneDTO();
                    person.setIdentite(CsvValueNormalizer.normalize(line[1]));
                    person.setDateNaissance(CsvValueNormalizer.normalize(line[2]));
                    person.setLieuNaissance(CsvValueNormalizer.normalize(line[3]));
                    person.setUrl(CsvValueNormalizer.normalize(line[5]));

                    ActeurDTO actor = new ActeurDTO();
                    actor.setIdImdb(imdbId);
                    actor.setTaille(CsvValueNormalizer.normalize(line[4]));
                    actor.setPersonne(person);
                    actorService.createActeur(actor);
                } catch (RuntimeException exception) {
                    LOGGER.warn("Failed to import actor from CSV row {}", rowNumber, exception);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Unable to read actor CSV file: " + filePath, exception);
        }
    }
}
