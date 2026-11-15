package com.example.imdbopen.actor.api;

import com.example.imdbopen.actor.application.ActeurService;
import exceptions.EntityNotFoundException;
import exceptions.InvalidDataException;
import web.model.dto.ActeurDTO;
import web.model.dto.PersonneDTO;
import web.model.generic.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActeurControllerTest {
    @InjectMocks
    private ActeurController acteurController;
    @Mock
    private ActeurService acteurService;
    private ActeurDTO acteurDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        PersonneDTO personneDTO = new PersonneDTO();
        personneDTO.setIdentite("Jean-Luc");
        personneDTO.setDateNaissance("1980-01-01");
        personneDTO.setLieuNaissance("Paris");
        personneDTO.setUrl("http://example.com");
        acteurDTO = new ActeurDTO();
        acteurDTO.setId(1L);
        acteurDTO.setIdImdb("nm1234567");
        acteurDTO.setTaille("180cm");
        acteurDTO.setPersonne(personneDTO);
    }

    @Test
    void createsActorSuccessfully() {
        when(acteurService.createActeur(any(ActeurDTO.class))).thenReturn(acteurDTO);
        ResponseEntity<ApiResponse<ActeurDTO>> response = acteurController.createActeur(acteurDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(acteurDTO.getId(), response.getBody().getData().getId());
        verify(acteurService).createActeur(any(ActeurDTO.class));
    }

    @Test
    void propagatesInvalidCreateData() {
        doThrow(new InvalidDataException("Invalid data")).when(acteurService).createActeur(any(ActeurDTO.class));
        InvalidDataException thrown = assertThrows(InvalidDataException.class,
                () -> acteurController.createActeur(acteurDTO));
        assertEquals("Invalid data", thrown.getMessage());
    }

    @Test
    void updatesActorSuccessfully() {
        when(acteurService.updateActeur(eq(1L), any(ActeurDTO.class))).thenReturn(acteurDTO);
        ResponseEntity<ApiResponse<ActeurDTO>> response = acteurController.updateActeur(1L, acteurDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(acteurDTO.getId(), response.getBody().getData().getId());
    }

    @Test
    void propagatesMissingActorOnUpdate() {
        doThrow(new EntityNotFoundException("Acteur not found"))
                .when(acteurService).updateActeur(eq(1L), any(ActeurDTO.class));
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> acteurController.updateActeur(1L, acteurDTO));
        assertEquals("Acteur not found", thrown.getMessage());
    }

    @Test
    void deletesActorSuccessfully() {
        ResponseEntity<ApiResponse<Void>> response = acteurController.deleteActeur(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(acteurService).deleteActeur(1L);
    }

    @Test
    void propagatesMissingActorOnDeletion() {
        doThrow(new EntityNotFoundException("Acteur not found")).when(acteurService).deleteActeur(1L);
        assertThrows(EntityNotFoundException.class, () -> acteurController.deleteActeur(1L));
    }
}
