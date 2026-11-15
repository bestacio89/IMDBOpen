package com.example.imdbopen.person.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BirthDateParser {
    private static final Pattern NUMERIC_DATE = Pattern.compile("^(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})$");
    private static final List<DateTimeFormatter> NAMED_FORMATTERS = List.of(
            formatter("MMMM d, uuuu", Locale.ENGLISH),
            formatter("d MMMM uuuu", Locale.ENGLISH),
            formatter("d MMMM uuuu", Locale.FRENCH),
            formatter("d MMM uuuu", Locale.ENGLISH),
            formatter("d MMM uuuu", Locale.FRENCH));

    private BirthDateParser() { }

    public static Optional<LocalDate> parse(String source) {
        if (source == null || source.isBlank()) return Optional.empty();
        String value = source.trim();
        try {
            return Optional.of(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException ignored) {
            // Try the explicitly supported source formats below.
        }

        Matcher numeric = NUMERIC_DATE.matcher(value);
        if (numeric.matches()) {
            int first = Integer.parseInt(numeric.group(1));
            int second = Integer.parseInt(numeric.group(2));
            int year = Integer.parseInt(numeric.group(3));
            if (first == second) return validDate(year, first, second);
            if (first > 12 && second <= 12) return validDate(year, second, first);
            if (second > 12 && first <= 12) return validDate(year, first, second);
            return Optional.empty();
        }

        for (DateTimeFormatter formatter : NAMED_FORMATTERS) {
            try {
                return Optional.of(LocalDate.parse(value, formatter));
            } catch (DateTimeParseException ignored) {
                // Continue until a supported locale format matches.
            }
        }
        return Optional.empty();
    }

    private static Optional<LocalDate> validDate(int year, int month, int day) {
        try {
            return Optional.of(LocalDate.of(year, month, day));
        } catch (java.time.DateTimeException ignored) {
            return Optional.empty();
        }
    }

    private static DateTimeFormatter formatter(String pattern, Locale locale) {
        return new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern)
                .toFormatter(locale).withResolverStyle(ResolverStyle.STRICT);
    }
}
