package com.example.imdbopen.ingestion.infrastructure.csv;

import com.example.imdbopen.casting.application.RoleService;
import com.example.imdbopen.casting.contracts.RoleDTO;
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
public class RoleExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleExtractor.class);
    private final RoleService roleService;

    public RoleExtractor(RoleService roleService) {
        this.roleService = roleService;
    }

    @Transactional
    public void extractRolesFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            reader.readNext();
            String[] line;
            long rowNumber = 1;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                if (line.length < 3) {
                    LOGGER.warn("Skipping role CSV row {}: expected 3 columns but found {}", rowNumber, line.length);
                    continue;
                }
                try {
                    RoleDTO role = new RoleDTO();
                    role.setFilmId(CsvValueNormalizer.normalize(line[0]));
                    role.setActeurId(CsvValueNormalizer.normalize(line[1]));
                    role.setRoleName(CsvValueNormalizer.normalize(line[2]));
                    roleService.createRoleIfAbsent(role);
                } catch (RuntimeException exception) {
                    LOGGER.warn("Failed to import role from CSV row {}", rowNumber, exception);
                }
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Unable to read role CSV file: " + filePath, exception);
        }
    }
}
