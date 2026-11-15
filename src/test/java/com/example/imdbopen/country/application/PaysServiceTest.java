package com.example.imdbopen.country.application;

import com.example.imdbopen.country.application.port.out.CountryStore;
import com.example.imdbopen.country.domain.Pays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaysServiceTest {
    @Mock
    private CountryStore countryStore;

    @InjectMocks
    private PaysService paysService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByIdDelegatesToStore() {
        Pays country = new Pays(1L, "France");
        when(countryStore.findById(1L)).thenReturn(Optional.of(country));

        Optional<Pays> result = paysService.findById(1L);

        assertEquals(Optional.of(country), result);
        verify(countryStore).findById(1L);
    }

    @Test
    void findOrCreateReturnsExistingCountry() {
        Pays country = new Pays(1L, "France");
        when(countryStore.findByName("France")).thenReturn(Optional.of(country));

        Pays result = paysService.findOrCreatePays("France");

        assertSame(country, result);
        verify(countryStore).findByName("France");
    }

    @Test
    void findOrCreatePersistsMissingCountry() {
        Pays country = new Pays(null, "France");
        Pays persisted = new Pays(1L, "France");
        when(countryStore.findByName("France")).thenReturn(Optional.empty());
        when(countryStore.save(country)).thenReturn(persisted);

        Pays result = paysService.findOrCreatePays("France");

        assertSame(persisted, result);
        verify(countryStore).save(country);
    }

    @Test
    void saveDelegatesToStore() {
        Pays country = new Pays(null, "France");
        when(countryStore.save(country)).thenReturn(country);

        assertSame(country, paysService.save(country));
        verify(countryStore).save(country);
    }
}
