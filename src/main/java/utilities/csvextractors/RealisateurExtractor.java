package utilities.csvextractors;

import com.example.imdbopen.person.domain.Personne;
import com.example.imdbopen.director.domain.Realisateur;
import persistence.repository.IRealisateurRepository;
import persistence.repository.IPersonneRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@Component
public class RealisateurExtractor {

    @Autowired
    private IRealisateurRepository realisateurRepository;

    @Autowired
    private IPersonneRepository personneRepository;

    @Transactional
    public void extractRealisateursFromCSV(String filePath) {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            reader.readNext();
            String[] line;

            while ((line = reader.readNext()) != null) {
                try {
                    String url = line[4].isEmpty() || "N/A".equals(line[4]) ? null : line[4].trim();
                    String identite = line[1].isEmpty() || "N/A".equals(line[1]) ? null : line[1].trim();
                    String dateNaissanceStr = line[2].isEmpty() || "N/A".equals(line[2]) ? null : line[2].trim();
                    String lieuNaissance = line[3].isEmpty() || "N/A".equals(line[3]) ? null : line[3].trim();
                    String imdbId = line[0].isEmpty() || "N/A".equals(line[0]) ? null : line[0].trim();

                    Optional<Personne> optionalPersonne =
                            personneRepository.findByIdentiteAndDateNaissance(identite, dateNaissanceStr);

                    Personne personne;
                    if (optionalPersonne.isPresent()) {
                        personne = optionalPersonne.get();
                    } else {
                        personne = new Personne();
                        personne.setIdentite(identite);
                        personne.setDateNaissance(dateNaissanceStr);
                        personne.setLieuNaissance(lieuNaissance);
                        personne.setUrl(url);

                        try {
                            personne = personneRepository.save(personne);
                            if (personne.getId() == null) {
                                throw new IllegalStateException("Failed to save Personne: ID is null after save.");
                            }
                        } catch (Exception e) {
                            System.err.println("Error saving Personne: " + identite + " - " + e.getMessage());
                            e.printStackTrace();
                            continue;
                        }
                    }

                    Optional<Realisateur> optionalRealisateur = realisateurRepository.findByPersonne(personne);
                    if (optionalRealisateur.isPresent()) {
                        System.out.println("Realisateur with Personne ID " + personne.getId() + " already exists. Skipping...");
                    } else {
                        Realisateur realisateur = new Realisateur();
                        realisateur.setPersonne(personne);
                        realisateur.setIdImdb(imdbId);

                        try {
                            realisateurRepository.save(realisateur);
                            realisateurRepository.flush();
                        } catch (Exception e) {
                            System.err.println("Error saving Realisateur: " + identite + " - " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line for realisateur: "
                            + (line.length > 1 ? line[0] : "Unknown") + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.err.println("CSV validation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
