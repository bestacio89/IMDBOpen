package entities.business.personne;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BirthDateParserTest {
    @Test
    void parsesIsoAndNamedMonthDates() {
        assertEquals(LocalDate.of(1990, 1, 2), BirthDateParser.parse("1990-01-02").orElseThrow());
        assertEquals(LocalDate.of(1990, 1, 2), BirthDateParser.parse("January 2, 1990").orElseThrow());
        assertEquals(LocalDate.of(1990, 1, 2), BirthDateParser.parse("2 janvier 1990").orElseThrow());
    }

    @Test
    void parsesUnambiguousNumericDates() {
        assertEquals(LocalDate.of(1990, 1, 13), BirthDateParser.parse("13/01/1990").orElseThrow());
        assertEquals(LocalDate.of(1990, 1, 13), BirthDateParser.parse("01/13/1990").orElseThrow());
    }

    @Test
    void rejectsAmbiguousOrPartialDatesInsteadOfGuessing() {
        assertTrue(BirthDateParser.parse("01/02/1990").isEmpty());
        assertTrue(BirthDateParser.parse("1990").isEmpty());
        assertTrue(BirthDateParser.parse("NA").isEmpty());
    }

    @Test
    void rejectsImpossibleDates() {
        assertTrue(BirthDateParser.parse("31/02/1990").isEmpty());
    }
}
