package com.example.demo;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Discovers the checked-in CSV contracts without assuming their headers or column semantics.
 */
class CsvDatasetDiscoveryTest {

    private static final Path DATASET_DIRECTORY = Path.of("src/main/resources/CSV");

    private static final String[] DATASETS = {
            "films.csv",
            "acteurs.csv",
            "roles.csv",
            "realisateurs.csv",
            "castingPrincipal.csv",
            "film_realisateurs.csv"
    };

    @Test
    void shouldReportCheckedInDatasetContracts() throws IOException, CsvValidationException {
        Map<String, List<String[]>> datasets = new HashMap<>();
        for (String dataset : DATASETS) {
            datasets.put(dataset, auditDataset(dataset));
        }
        auditRelationships(datasets);
        auditDomainFields(datasets);
        auditSqlDump();
    }

    private List<String[]> auditDataset(String filename) throws IOException, CsvValidationException {
        Path path = DATASET_DIRECTORY.resolve(filename);
        assertTrue(Files.isRegularFile(path), "Missing dataset: " + path);
        assertTrue(Files.size(path) > 0, "Empty dataset: " + path);

        byte[] bytes = Files.readAllBytes(path);
        String content;
        String encoding;

        try {
            content = decode(bytes, StandardCharsets.UTF_8);
            encoding = StandardCharsets.UTF_8.name();
        } catch (CharacterCodingException utf8Exception) {
            try {
                Charset windows1252 = Charset.forName("windows-1252");
                content = decode(bytes, windows1252);
                encoding = windows1252.name();
            } catch (CharacterCodingException windows1252Exception) {
                content = decode(bytes, StandardCharsets.ISO_8859_1);
                encoding = StandardCharsets.ISO_8859_1.name();
            }
        }

        try (Reader source = new StringReader(content);
             CSVReader csv = new CSVReaderBuilder(source)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .build()) {
            String[] header = csv.readNext();
            assertTrue(header != null && header.length > 0, "Missing CSV header: " + path);
            header[0] = header[0].replace("\uFEFF", "");

            List<String[]> rows = new ArrayList<>();
            long records = 0;
            int minimumColumns = Integer.MAX_VALUE;
            int maximumColumns = 0;
            long[] emptyValues = new long[header.length];
            long replacementCharacters = 0;
            String[] row;

            System.out.printf("CSV_AUDIT|file=%s|bytes=%d|encoding=%s|header=%s%n",
                    filename, Files.size(path), encoding, Arrays.toString(header));

            while ((row = csv.readNext()) != null) {
                records++;
                rows.add(row);
                replacementCharacters += Arrays.stream(row)
                        .filter(value -> value != null)
                        .mapToLong(value -> value.chars().filter(character -> character == 0xFFFD).count())
                        .sum();
                minimumColumns = Math.min(minimumColumns, row.length);
                maximumColumns = Math.max(maximumColumns, row.length);

                for (int column = 0; column < Math.min(row.length, emptyValues.length); column++) {
                    if (row[column] == null || row[column].isBlank()) {
                        emptyValues[column]++;
                    }
                }

                if (records <= 3) {
                    System.out.printf("CSV_SAMPLE|file=%s|record=%d|values=%s%n",
                            filename, records, formatSample(row));
                }
            }

            assertFalse(records == 0, "Dataset has no data records: " + path);
            System.out.printf(
                    "CSV_SUMMARY|file=%s|records=%d|minColumns=%d|maxColumns=%d|replacementCharacters=%d|emptyByColumn=%s%n",
                    filename, records, minimumColumns, maximumColumns, replacementCharacters,
                    Arrays.toString(emptyValues));
            return rows;
        }
    }

    private void auditDomainFields(Map<String, List<String[]>> datasets) {
        auditFilmFields(datasets.get("films.csv"));
        auditPersonFields("acteurs.csv", datasets.get("acteurs.csv"), true);
        auditPersonFields("realisateurs.csv", datasets.get("realisateurs.csv"), false);
    }

    private void auditFilmFields(List<String[]> rows) {
        Set<String> years = new HashSet<>();
        Set<String> countries = new HashSet<>();
        Set<String> languages = new HashSet<>();
        Set<String> genres = new HashSet<>();
        long invalidYears = 0;
        long invalidRatings = 0;
        List<String> invalidYearSamples = new ArrayList<>();
        List<String> invalidRatingSamples = new ArrayList<>();
        BigDecimal minimumRating = null;
        BigDecimal maximumRating = null;

        for (String[] row : rows) {
            String year = row[2].trim();
            years.add(year);
            try {
                Integer.parseInt(year);
            } catch (NumberFormatException exception) {
                invalidYears++;
                if (invalidYearSamples.size() < 10) {
                    invalidYearSamples.add(year);
                }
            }

            try {
                BigDecimal rating = new BigDecimal(row[3].trim().replace(',', '.'));
                minimumRating = minimumRating == null || rating.compareTo(minimumRating) < 0
                        ? rating : minimumRating;
                maximumRating = maximumRating == null || rating.compareTo(maximumRating) > 0
                        ? rating : maximumRating;
            } catch (NumberFormatException exception) {
                invalidRatings++;
                if (invalidRatingSamples.size() < 10) {
                    invalidRatingSamples.add(row[3].trim());
                }
            }

            countries.add(row[9].trim());
            languages.add(row[7].trim());
            for (String genre : row[6].split(",")) {
                if (!genre.isBlank() && !genre.equalsIgnoreCase("None")) {
                    genres.add(genre.trim());
                }
            }
        }

        System.out.printf(
                "CSV_FILM_PROFILE|years=%d|invalidYears=%d|invalidYearSamples=%s|ratingInvalid=%d|invalidRatingSamples=%s|ratingMin=%s|ratingMax=%s|countries=%d|languages=%d|genres=%d|languageNone=%d%n",
                years.size(), invalidYears, invalidYearSamples, invalidRatings, invalidRatingSamples,
                minimumRating, maximumRating, countries.size(), languages.size(), genres.size(),
                rows.stream().filter(row -> row[7].trim().equalsIgnoreCase("None")).count());
    }

    private void auditPersonFields(String filename, List<String[]> rows, boolean hasHeight) {
        DateTimeFormatter[] dateFormatters = {
                DateTimeFormatter.ofPattern("MMMM d uuuu", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.FRENCH),
                DateTimeFormatter.ofPattern("d MMM uuuu", Locale.FRENCH)
        };
        Pattern heightPattern = Pattern.compile("^([0-9]+([.][0-9]+)?) *m$",
                Pattern.CASE_INSENSITIVE);
        Set<String> birthPlaces = new HashSet<>();
        long missingDates = 0;
        long invalidDates = 0;
        long missingHeights = 0;
        long invalidHeights = 0;
        BigDecimal minimumHeight = null;
        BigDecimal maximumHeight = null;
        List<String> invalidDateSamples = new ArrayList<>();
        List<String> invalidHeightSamples = new ArrayList<>();

        for (String[] row : rows) {
            String birthDate = row[2].trim();
            if (birthDate.isBlank() || birthDate.equalsIgnoreCase("NA")
                    || birthDate.equalsIgnoreCase("N/A")) {
                missingDates++;
            } else if (!isValidDate(birthDate, dateFormatters)) {
                invalidDates++;
                if (invalidDateSamples.size() < 10) {
                    invalidDateSamples.add(birthDate);
                }
            }
            birthPlaces.add(row[3].trim());

            if (hasHeight) {
                String height = row[4].trim();
                var matcher = heightPattern.matcher(height);
                if (height.equalsIgnoreCase("NA") || height.equalsIgnoreCase("N/A")) {
                    missingHeights++;
                } else if (matcher.matches()) {
                    BigDecimal value = new BigDecimal(matcher.group(1));
                    minimumHeight = minimumHeight == null || value.compareTo(minimumHeight) < 0
                            ? value : minimumHeight;
                    maximumHeight = maximumHeight == null || value.compareTo(maximumHeight) > 0
                            ? value : maximumHeight;
                } else {
                    invalidHeights++;
                    if (invalidHeightSamples.size() < 5) {
                        invalidHeightSamples.add(height);
                    }
                }
            }
        }

        System.out.printf(
                "CSV_PERSON_PROFILE|file=%s|records=%d|missingDates=%d|invalidDates=%d|birthPlaces=%d|missingHeights=%d|invalidHeights=%d|heightMinMeters=%s|heightMaxMeters=%s|dateSamples=%s|heightSamples=%s%n",
                filename, rows.size(), missingDates, invalidDates, birthPlaces.size(), missingHeights,
                invalidHeights, minimumHeight, maximumHeight, invalidDateSamples, invalidHeightSamples);
    }

    private boolean isValidDate(String value, DateTimeFormatter[] formatters) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate.parse(value, formatter);
                return true;
            } catch (DateTimeParseException exception) {
                // Try the next known source format.
            }
        }
        return false;
    }

    private void auditRelationships(Map<String, List<String[]>> datasets) {
        Set<String> filmIds = collectIdentifiers(datasets.get("films.csv"), 0);
        Set<String> actorIds = collectIdentifiers(datasets.get("acteurs.csv"), 0);
        Set<String> directorIds = collectIdentifiers(datasets.get("realisateurs.csv"), 0);
        Set<String> sharedPersonIds = new HashSet<>(actorIds);
        sharedPersonIds.retainAll(directorIds);
        System.out.printf("CSV_SHARED_PERSON_IDS|actorIds=%d|directorIds=%d|sharedIds=%d%n",
                actorIds.size(), directorIds.size(), sharedPersonIds.size());

        reportRelationship("roles.csv", datasets.get("roles.csv"), filmIds, actorIds);
        reportRelationship("castingPrincipal.csv", datasets.get("castingPrincipal.csv"), filmIds, actorIds);
        reportRelationship("film_realisateurs.csv", datasets.get("film_realisateurs.csv"),
                filmIds, directorIds);
        reportPairCardinality("roles.csv", datasets.get("roles.csv"));
        reportPairCardinality("castingPrincipal.csv", datasets.get("castingPrincipal.csv"));
        reportPairCardinality("film_realisateurs.csv", datasets.get("film_realisateurs.csv"));
        reportPrincipalCastOverlap(datasets.get("roles.csv"), datasets.get("castingPrincipal.csv"));
        reportDuplicates("films.csv", datasets.get("films.csv"), 0);
        reportDuplicates("acteurs.csv", datasets.get("acteurs.csv"), 0);
        reportDuplicates("realisateurs.csv", datasets.get("realisateurs.csv"), 0);
    }

    private Set<String> collectIdentifiers(List<String[]> rows, int column) {
        Set<String> identifiers = new HashSet<>();
        for (String[] row : rows) {
            if (row.length > column && !row[column].isBlank()) {
                identifiers.add(row[column].trim());
            }
        }
        return identifiers;
    }

    private void reportRelationship(
            String filename, List<String[]> rows, Set<String> leftIds, Set<String> rightIds) {
        long missingLeft = rows.stream()
                .filter(row -> row.length > 0 && !leftIds.contains(row[0].trim()))
                .count();
        long missingRight = rows.stream()
                .filter(row -> row.length > 1 && !rightIds.contains(row[1].trim()))
                .count();
        System.out.printf("CSV_RELATIONSHIP|file=%s|records=%d|missingLeft=%d|missingRight=%d%n",
                filename, rows.size(), missingLeft, missingRight);
    }

    private void reportPairCardinality(String filename, List<String[]> rows) {
        Map<String, Set<String>> charactersByCredit = new HashMap<>();
        Map<String, Long> rowsByCredit = new HashMap<>();
        for (String[] row : rows) {
            if (row.length > 1) {
                String credit = row[0].trim() + "|" + row[1].trim();
                rowsByCredit.merge(credit, 1L, Long::sum);
                if (row.length > 2 && !row[2].isBlank()) {
                    charactersByCredit.computeIfAbsent(credit, key -> new HashSet<>()).add(row[2].trim());
                }
            }
        }

        long creditsWithMultipleRows = rowsByCredit.values().stream()
                .filter(count -> count > 1)
                .count();
        long creditsWithMultipleCharacters = charactersByCredit.values().stream()
                .filter(characters -> characters.size() > 1)
                .count();
        long maximumRowsPerCredit = rowsByCredit.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        System.out.printf(
                "CSV_PAIR_CARDINALITY|file=%s|uniquePairs=%d|pairsWithMultipleRows=%d|pairsWithMultipleCharacters=%d|maxRowsPerPair=%d%n",
                filename, rowsByCredit.size(), creditsWithMultipleRows,
                creditsWithMultipleCharacters, maximumRowsPerCredit);
    }

    private void reportPrincipalCastOverlap(
            List<String[]> roleRows, List<String[]> principalRows) {
        Set<String> roleCredits = collectPairs(roleRows);
        Set<String> principalCredits = collectPairs(principalRows);
        long principalWithoutRole = principalCredits.stream()
                .filter(credit -> !roleCredits.contains(credit))
                .count();
        long rolesOutsidePrincipal = roleCredits.stream()
                .filter(credit -> !principalCredits.contains(credit))
                .count();
        System.out.printf(
                "CSV_CAST_OVERLAP|roleCredits=%d|principalCredits=%d|principalWithoutRole=%d|rolesOutsidePrincipal=%d%n",
                roleCredits.size(), principalCredits.size(), principalWithoutRole,
                rolesOutsidePrincipal);
    }

    private Set<String> collectPairs(List<String[]> rows) {
        Set<String> pairs = new HashSet<>();
        for (String[] row : rows) {
            if (row.length > 1) {
                pairs.add(row[0].trim() + "|" + row[1].trim());
            }
        }
        return pairs;
    }

    private void reportDuplicates(String filename, List<String[]> rows, int column) {
        Map<String, Long> rowCountsByIdentifier = new HashMap<>();
        Map<String, Set<String>> distinctRowsByIdentifier = new HashMap<>();
        for (String[] row : rows) {
            if (row.length > column && !row[column].isBlank()) {
                String identifier = row[column].trim();
                rowCountsByIdentifier.merge(identifier, 1L, Long::sum);
                distinctRowsByIdentifier.computeIfAbsent(identifier, key -> new HashSet<>())
                        .add(Arrays.toString(row));
            }
        }

        long duplicateIdentifiers = rowCountsByIdentifier.values().stream()
                .filter(count -> count > 1)
                .count();
        long conflictingIdentifiers = distinctRowsByIdentifier.values().stream()
                .filter(records -> records.size() > 1)
                .count();
        long duplicateRows = rows.size() - rowCountsByIdentifier.size();
        System.out.printf(
                "CSV_IDENTIFIERS|file=%s|unique=%d|duplicateRows=%d|duplicateIdentifiers=%d|conflictingIdentifiers=%d%n",
                filename, rowCountsByIdentifier.size(), duplicateRows, duplicateIdentifiers,
                conflictingIdentifiers);
    }

    private void auditSqlDump() throws IOException {
        Path dump = Path.of("src/main/resources/db/dump-testimdb.sql");
        assertTrue(Files.isRegularFile(dump), "Missing SQL dump: " + dump);
        assertTrue(Files.size(dump) > 0, "Empty SQL dump: " + dump);

        long lines = 0;
        long createTableStatements = 0;
        long insertStatements = 0;
        List<String> tableDefinitions = new ArrayList<>();
        StringBuilder currentDefinition = new StringBuilder();
        boolean capturingTableDefinition = false;

        try (BufferedReader reader = Files.newBufferedReader(dump, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                String statement = line.stripLeading();
                String normalized = statement.toUpperCase(Locale.ROOT);
                if (normalized.startsWith("CREATE TABLE")) {
                    createTableStatements++;
                    capturingTableDefinition = true;
                    currentDefinition.setLength(0);
                }
                if (capturingTableDefinition) {
                    currentDefinition.append(statement.trim()).append(' ');
                    if (statement.contains(";")) {
                        tableDefinitions.add(currentDefinition.toString().trim());
                        capturingTableDefinition = false;
                    }
                }
                if (normalized.startsWith("INSERT INTO")) {
                    insertStatements++;

                }
            }
        }

        System.out.printf(
                "SQL_DUMP_AUDIT|bytes=%d|lines=%d|createTables=%d|insertStatements=%d%n",
                Files.size(dump), lines, createTableStatements, insertStatements);
        tableDefinitions.forEach(definition -> System.out.println("SQL_DUMP_TABLE|" + definition));

        Map<String, Long> insertedRowsByTable = new HashMap<>();
        String sql = Files.readString(dump, StandardCharsets.UTF_8);
        for (String sqlStatement : splitSqlStatements(sql)) {
            String normalized = sqlStatement.stripLeading().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("INSERT INTO")) {
                String table = extractInsertTableName(sqlStatement);
                insertedRowsByTable.merge(table, countInsertedRows(sqlStatement), Long::sum);
            }
        }
        insertedRowsByTable.forEach((table, rows) ->
                System.out.printf("SQL_DUMP_INSERT|table=%s|rows=%d%n", table, rows));
    }

    private List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        boolean escaped = false;
        int statementStart = 0;

        for (int index = 0; index < sql.length(); index++) {
            char character = sql.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inSingleQuote || inDoubleQuote) && character == 92) {
                escaped = true;
                continue;
            }
            if (!inDoubleQuote && !inBacktick && character == 39) {
                if (inSingleQuote && index + 1 < sql.length() && sql.charAt(index + 1) == 39) {
                    index++;
                } else {
                    inSingleQuote = !inSingleQuote;
                }
            } else if (!inSingleQuote && !inBacktick && character == 34) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote && character == 96) {
                inBacktick = !inBacktick;
            } else if (!inSingleQuote && !inDoubleQuote && !inBacktick && character == 59) {
                statements.add(sql.substring(statementStart, index));
                statementStart = index + 1;
            }
        }
        if (statementStart < sql.length()) {
            statements.add(sql.substring(statementStart));
        }
        return statements;
    }

    private String extractInsertTableName(String statement) {
        String remainder = statement.stripLeading().substring("INSERT INTO".length()).trim();
        if (remainder.startsWith("`")) {
            int closingQuote = remainder.indexOf('`', 1);
            return remainder.substring(1, closingQuote);
        }
        return remainder.split("\\s|\\(", 2)[0];
    }

    private long countInsertedRows(String statement) {
        int valuesIndex = statement.toUpperCase(Locale.ROOT).indexOf("VALUES");
        if (valuesIndex < 0) {
            return 0;
        }

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;
        int depth = 0;
        long rows = 0;
        String values = statement.substring(valuesIndex + "VALUES".length());

        for (int index = 0; index < values.length(); index++) {
            char character = values.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inSingleQuote || inDoubleQuote) && character == 92) {
                escaped = true;
                continue;
            }
            if (!inDoubleQuote && character == 39) {
                if (inSingleQuote && index + 1 < values.length() && values.charAt(index + 1) == 39) {
                    index++;
                } else {
                    inSingleQuote = !inSingleQuote;
                }
            } else if (!inSingleQuote && character == 34) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote && character == 40) {
                if (depth == 0) {
                    rows++;
                }
                depth++;
            } else if (!inSingleQuote && !inDoubleQuote && character == 41) {
                depth--;
            }
        }
        return rows;
    }

    private String decode(byte[] bytes, Charset charset) throws CharacterCodingException {
        var decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }

    private String formatSample(String[] row) {
        String[] values = Arrays.stream(row)
                .map(value -> value == null ? "" : value.replaceAll("\\s+", " ").trim())
                .map(value -> value.length() > 80 ? value.substring(0, 80) + "…" : value)
                .toArray(String[]::new);
        return Arrays.toString(values);
    }
}
