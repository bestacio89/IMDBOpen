package entities.business.Film;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FilmYearParsingTest {
    @Test
    void preservesSourceAndExtractsSingleYear() {
        Film film = new Film();
        film.setAnnee("1998");
        assertEquals("1998", film.getAnnee());
        assertEquals(Short.valueOf((short) 1998), film.getReleaseYearStart());
        assertEquals(Short.valueOf((short) 1998), film.getReleaseYearEnd());
    }

    @Test
    void preservesSourceAndExtractsYearRange() {
        Film film = new Film();
        film.setAnnee("1952�1953");
        assertEquals("1952�1953", film.getAnnee());
        assertEquals(Short.valueOf((short) 1952), film.getReleaseYearStart());
        assertEquals(Short.valueOf((short) 1953), film.getReleaseYearEnd());
    }

    @Test
    void leavesUnparseableSourceWithoutInventingYears() {
        Film film = new Film();
        film.setAnnee("unknown");
        assertEquals("unknown", film.getAnnee());
        assertNull(film.getReleaseYearStart());
        assertNull(film.getReleaseYearEnd());
    }
}
