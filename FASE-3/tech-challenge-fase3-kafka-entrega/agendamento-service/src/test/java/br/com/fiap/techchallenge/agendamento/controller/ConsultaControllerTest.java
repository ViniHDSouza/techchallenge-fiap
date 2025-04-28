package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.model.*;
import br.com.fiap.techchallenge.agendamento.service.ConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para a classe ConsultaController.
 */
@WebMvcTest(ConsultaController.class)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultaService consultaService;

    private ObjectMapper objectMapper;
    private Paciente paciente;
    private Medico medico;
    private Consulta consulta1;
    private Consulta consulta2;

    @BeforeEach
    void setUp() {
        // Configura o ObjectMapper para lidar com LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Dados de teste
        paciente = new Paciente(1L, "Paciente Teste", "paciente@teste.com", "11999998888");
        medico = new Medico(1L, "Dr. Teste", "Cardiologia", "CRM12345");
        consulta1 = new Consulta(1L, paciente, medico, null,
                LocalDateTime.now().plusDays(1), StatusConsulta.AGENDADA, "Observação 1");
        consulta2 = new Consulta(2L, paciente, medico, null,
                LocalDateTime.now().plusDays(2), StatusConsulta.AGENDADA, "Observação 2");
    }

    @Test
    void deveListarTodasConsultasComSucesso() throws Exception {
        // Arrange
        List<Consulta> consultas = Arrays.asList(consulta1, consulta2);
        when(consultaService.listarTodasConsultas()).thenReturn(consultas);

        // Act & Assert
        mockMvc.perform(get("/consultas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(consulta1.getId()))
                .andExpect(jsonPath("$[1].id").value(consulta2.getId()));

        verify(consultaService, times(1)).listarTodasConsultas();
    }

    @Test
    void deveBuscarConsultaPorIdComSucesso() throws Exception {
        // Arrange
        when(consultaService.buscarConsultaPorId(1L)).thenReturn(Optional.of(consulta1));

        // Act & Assert
        mockMvc.perform(get("/consultas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consulta1.getId()))
                .andExpect(jsonPath("$.observacoes").value(consulta1.getObservacoes()));

        verify(consultaService, times(1)).buscarConsultaPorId(1L);
    }

    @Test
    void deveRetornarNotFoundAoBuscarConsultaPorIdInexistente() throws Exception {
        // Arrange
        when(consultaService.buscarConsultaPorId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/consultas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(consultaService, times(1)).buscarConsultaPorId(99L);
    }

    @Test
    void deveCriarConsultaComSucesso() throws Exception {
        // Arrange
        Consulta novaConsultaInput = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(5), StatusConsulta.AGENDADA, "Nova consulta");
        Consulta consultaSalva = new Consulta(3L, paciente, medico, null,
                novaConsultaInput.getDataHora(), StatusConsulta.AGENDADA, "Nova consulta");

        when(consultaService.salvarConsulta(any(Consulta.class))).thenReturn(consultaSalva);

        // Act & Assert
        mockMvc.perform(post("/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novaConsultaInput)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consultaSalva.getId()))
                .andExpect(jsonPath("$.status").value(StatusConsulta.AGENDADA.name()));

        verify(consultaService, times(1)).salvarConsulta(any(Consulta.class));
    }

    @Test
    void deveRetornarBadRequestAoCriarConsultaComDadosInvalidos() throws Exception {
        // Arrange
        Consulta consultaInvalida = new Consulta(null, null, null, null, null, null, null); // Dados inválidos

        // Act & Assert
        mockMvc.perform(post("/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultaInvalida)))
                .andExpect(status().isBadRequest()); // Espera 400 devido à validação @Valid

        verify(consultaService, never()).salvarConsulta(any(Consulta.class));
    }
    
    @Test
    void deveRetornarBadRequestAoCriarConsultaComEntidadeRelacionadaInexistente() throws Exception {
        // Arrange
        Consulta novaConsultaInput = new Consulta(null, new Paciente(99L, null, null, null), medico, null,
                LocalDateTime.now().plusDays(5), StatusConsulta.AGENDADA, "Nova consulta");

        when(consultaService.salvarConsulta(any(Consulta.class)))
            .thenThrow(new EntityNotFoundException("Paciente não encontrado com ID: 99"));

        // Act & Assert
        mockMvc.perform(post("/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novaConsultaInput)))
                .andExpect(status().isBadRequest());

        verify(consultaService, times(1)).salvarConsulta(any(Consulta.class));
    }

    @Test
    void deveAtualizarConsultaComSucesso() throws Exception {
        // Arrange
        Consulta dadosAtualizacao = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(10), StatusConsulta.AGENDADA, "Observação atualizada");
        Consulta consultaAtualizada = new Consulta(1L, paciente, medico, null,
                dadosAtualizacao.getDataHora(), StatusConsulta.AGENDADA, "Observação atualizada");

        when(consultaService.atualizarConsulta(eq(1L), any(Consulta.class))).thenReturn(consultaAtualizada);

        // Act & Assert
        mockMvc.perform(put("/consultas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dadosAtualizacao)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consultaAtualizada.getId()))
                .andExpect(jsonPath("$.observacoes").value(consultaAtualizada.getObservacoes()));

        verify(consultaService, times(1)).atualizarConsulta(eq(1L), any(Consulta.class));
    }

    @Test
    void deveRetornarNotFoundAoAtualizarConsultaInexistente() throws Exception {
        // Arrange
        Consulta dadosAtualizacao = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(10), StatusConsulta.AGENDADA, "Observação atualizada");

        when(consultaService.atualizarConsulta(eq(99L), any(Consulta.class)))
                .thenThrow(new EntityNotFoundException("Consulta não encontrada com ID: 99"));

        // Act & Assert
        mockMvc.perform(put("/consultas/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dadosAtualizacao)))
                .andExpect(status().isNotFound());

        verify(consultaService, times(1)).atualizarConsulta(eq(99L), any(Consulta.class));
    }

    @Test
    void deveExcluirConsultaComSucesso() throws Exception {
        // Arrange
        doNothing().when(consultaService).excluirConsulta(1L);

        // Act & Assert
        mockMvc.perform(delete("/consultas/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(consultaService, times(1)).excluirConsulta(1L);
    }

    @Test
    void deveRetornarNotFoundAoExcluirConsultaInexistente() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Consulta não encontrada com ID: 99"))
                .when(consultaService).excluirConsulta(99L);

        // Act & Assert
        mockMvc.perform(delete("/consultas/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(consultaService, times(1)).excluirConsulta(99L);
    }
}

