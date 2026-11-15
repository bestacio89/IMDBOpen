package com.example.imdbopen.country.api;

import com.example.imdbopen.country.application.PaysService;
import com.example.imdbopen.country.domain.Pays;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.imdbopen.country.contracts.PaysDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaysControllerTest {
    private MockMvc mockMvc;

    @Mock
    private PaysService paysService;

    @InjectMocks
    private PaysController paysController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paysController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllPaysReturnsCountries() throws Exception {
        Pays france = new Pays(1L, "France");
        Pays germany = new Pays(2L, "Germany");
        when(paysService.findAll()).thenReturn(Arrays.asList(france, germany));

        mockMvc.perform(get("/api/pays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nom", is("France")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].nom", is("Germany")));

        verify(paysService, times(1)).findAll();
    }

    @Test
    void getPaysByIdReturnsCountry() throws Exception {
        Pays country = new Pays(1L, "France");
        when(paysService.findById(1L)).thenReturn(Optional.of(country));

        mockMvc.perform(get("/api/pays/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("France")));

        verify(paysService).findById(1L);
    }

    @Test
    void getPaysByIdReturnsNotFound() throws Exception {
        when(paysService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pays/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(paysService).findById(1L);
    }

    @Test
    void createPaysReturnsPersistedCountry() throws Exception {
        PaysDTO request = new PaysDTO();
        request.setNom("France");
        when(paysService.save(any(Pays.class))).thenReturn(new Pays(1L, "France"));

        mockMvc.perform(post("/api/pays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("France")));

        verify(paysService).save(any(Pays.class));
    }

    @Test
    void updatePaysUpdatesExistingCountry() throws Exception {
        PaysDTO request = new PaysDTO();
        request.setNom("Updated Pays");
        when(paysService.findById(1L)).thenReturn(Optional.of(new Pays(1L, "France")));
        when(paysService.save(any(Pays.class))).thenReturn(new Pays(1L, "Updated Pays"));

        mockMvc.perform(put("/api/pays/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nom", is("Updated Pays")));

        verify(paysService).findById(1L);
        verify(paysService).save(any(Pays.class));
    }

    @Test
    void updatePaysReturnsNotFoundWhenMissing() throws Exception {
        PaysDTO request = new PaysDTO();
        request.setNom("Updated Pays");
        when(paysService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/pays/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(paysService).findById(1L);
    }

    @Test
    void deletePaysDeletesExistingCountry() throws Exception {
        when(paysService.findById(1L)).thenReturn(Optional.of(new Pays(1L, "France")));

        mockMvc.perform(delete("/api/pays/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(paysService).findById(1L);
        verify(paysService).deleteById(1L);
    }

    @Test
    void deletePaysReturnsNotFoundWhenMissing() throws Exception {
        when(paysService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/pays/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(paysService).findById(1L);
    }
}
