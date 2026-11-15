package com.example.imdbopen.director.api;

import com.example.imdbopen.director.application.RealisateurService;
import web.model.dto.RealisateurDTO;
import web.model.generic.ApiResponse;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealisateurControllerTest {
    @InjectMocks
    private RealisateurController realisateurController;
    @Mock
    private RealisateurService realisateurService;
    private RealisateurDTO realisateurDTO;

    @BeforeEach
    void setup() {
        realisateurDTO = new RealisateurDTO();
        realisateurDTO.setId(1L);
        realisateurDTO.setIdImdb("imdb12345");
    }

    @Test
    void getsAllDirectors() {
        when(realisateurService.findAll()).thenReturn(List.of(realisateurDTO));
        ResponseEntity<ApiResponse<List<RealisateurDTO>>> response = realisateurController.getAllRealisateurs();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        verify(realisateurService).findAll();
    }

    @Test
    void getsDirectorById() {
        when(realisateurService.findById(1L)).thenReturn(Optional.of(realisateurDTO));
        ResponseEntity<ApiResponse<RealisateurDTO>> response = realisateurController.getRealisateurById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void returnsNotFoundWhenDirectorIdDoesNotExist() {
        when(realisateurService.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> realisateurController.getRealisateurById(1L));
    }

    @Test
    void getsDirectorByImdbIdentifier() {
        when(realisateurService.findByIdImdb("imdb12345")).thenReturn(Optional.of(realisateurDTO));
        ResponseEntity<ApiResponse<RealisateurDTO>> response =
                realisateurController.getRealisateursByIdImdb("imdb12345");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void returnsNotFoundWhenImdbIdentifierDoesNotExist() {
        when(realisateurService.findByIdImdb("imdb12345")).thenReturn(Optional.empty());
        ResponseEntity<ApiResponse<RealisateurDTO>> response =
                realisateurController.getRealisateursByIdImdb("imdb12345");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody().getData());
    }

    @Test
    void createsDirector() {
        when(realisateurService.save(any(RealisateurDTO.class))).thenReturn(realisateurDTO);
        ResponseEntity<ApiResponse<RealisateurDTO>> response =
                realisateurController.createRealisateur(realisateurDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(realisateurService).save(realisateurDTO);
    }

    @Test
    void propagatesInvalidCreateData() {
        doThrow(new InvalidDataException("Invalid data")).when(realisateurService).save(any(RealisateurDTO.class));
        assertThrows(InvalidDataException.class, () -> realisateurController.createRealisateur(realisateurDTO));
    }

    @Test
    void updatesDirector() {
        when(realisateurService.update(any(RealisateurDTO.class))).thenReturn(realisateurDTO);
        ResponseEntity<ApiResponse<RealisateurDTO>> response =
                realisateurController.updateRealisateur(1L, realisateurDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(realisateurService).update(realisateurDTO);
    }

    @Test
    void rejectsUpdateWithMismatchedIdentifier() {
        realisateurDTO.setId(2L);
        assertThrows(InvalidDataException.class,
                () -> realisateurController.updateRealisateur(1L, realisateurDTO));
        verify(realisateurService, never()).update(any(RealisateurDTO.class));
    }

    @Test
    void deletesDirector() {
        ResponseEntity<ApiResponse<Void>> response = realisateurController.deleteRealisateur(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(realisateurService).deleteById(1L);
    }

    @Test
    void propagatesMissingDirectorOnDeletion() {
        doThrow(new EntityNotFoundException("Realisateur not found with ID: 1"))
                .when(realisateurService).deleteById(1L);
        assertThrows(EntityNotFoundException.class, () -> realisateurController.deleteRealisateur(1L));
    }
}
