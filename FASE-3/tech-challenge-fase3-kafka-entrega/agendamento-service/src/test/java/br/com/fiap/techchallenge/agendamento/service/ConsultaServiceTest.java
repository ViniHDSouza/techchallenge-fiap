package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.model.*;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.EnfermeiroRepository;
import br.com.fiap.techchallenge.agendamento.repository.MedicoRepository;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe ConsultaService.
 */
@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private EnfermeiroRepository enfermeiroRepository;
    @Mock
    private AgendamentoProducer agendamentoProducer;
    @Mock
    private Logger logger; // Mock do Logger

    @InjectMocks
    private ConsultaService consultaService;

    private Paciente paciente;
    private Medico medico;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        // Injeta o logger mockado
        ReflectionTestUtils.setField(ConsultaService.class, "logger", logger);

        // Dados de teste comuns
        paciente = new Paciente(1L, "Paciente Teste", "paciente@teste.com", "11999998888");
        medico = new Medico(1L, "Dr. Teste", "Cardiologia", "CRM12345");
        consulta = new Consulta(1L, paciente, medico, null,
                LocalDateTime.now().plusDays(1), StatusConsulta.AGENDADA, "Observação inicial");
    }

    @Test
    void deveListarTodasConsultas() {
        // Arrange
        when(consultaRepository.findAll()).thenReturn(Arrays.asList(consulta));

        // Act
        List<Consulta> resultado = consultaService.listarTodasConsultas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(consulta, resultado.get(0));
        verify(consultaRepository, times(1)).findAll();
        verify(logger, times(1)).debug("Listando todas as consultas");
    }

    @Test
    void deveBuscarConsultaPorIdComSucesso() {
        // Arrange
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        // Act
        Optional<Consulta> resultado = consultaService.buscarConsultaPorId(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(consulta, resultado.get());
        verify(consultaRepository, times(1)).findById(1L);
        verify(logger, times(1)).debug("Buscando consulta com ID: {}", 1L);
    }

    @Test
    void deveRetornarEmptyQuandoBuscarConsultaPorIdNaoExistente() {
        // Arrange
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Consulta> resultado = consultaService.buscarConsultaPorId(99L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(consultaRepository, times(1)).findById(99L);
        verify(logger, times(1)).debug("Buscando consulta com ID: {}", 99L);
    }

    @Test
    void deveSalvarConsultaComSucessoEEnviarMensagemKafka() {
        // Arrange
        Consulta novaConsulta = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(5), StatusConsulta.AGENDADA, "Nova consulta");
        Consulta consultaSalva = new Consulta(2L, paciente, medico, null,
                novaConsulta.getDataHora(), StatusConsulta.AGENDADA, "Nova consulta");

        // Mock para validação das entidades relacionadas
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        // Mock para salvar a consulta
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);
        // Mock para envio Kafka (não faz nada)
        doNothing().when(agendamentoProducer).enviarMensagemConsulta(any(ConsultaNotificacaoDTO.class));

        // Act
        Consulta resultado = consultaService.salvarConsulta(novaConsulta);

        // Assert
        assertNotNull(resultado);
        assertEquals(consultaSalva.getId(), resultado.getId());
        assertEquals(StatusConsulta.AGENDADA, resultado.getStatus());

        // Verifica se as validações e o save foram chamados
        verify(pacienteRepository, times(1)).findById(paciente.getId());
        verify(medicoRepository, times(1)).findById(medico.getId());
        verify(enfermeiroRepository, never()).findById(anyLong()); // Enfermeiro não foi informado
        verify(consultaRepository, times(1)).save(any(Consulta.class));

        // Verifica se a mensagem Kafka foi enviada
        ArgumentCaptor<ConsultaNotificacaoDTO> dtoCaptor = ArgumentCaptor.forClass(ConsultaNotificacaoDTO.class);
        verify(agendamentoProducer, times(1)).enviarMensagemConsulta(dtoCaptor.capture());
        assertEquals(consultaSalva.getId(), dtoCaptor.getValue().getId());
        assertEquals("CRIACAO", dtoCaptor.getValue().getTipoNotificacao());

        // Verifica logs
        verify(logger, times(1)).info("Tentando salvar nova consulta para o paciente ID: {}", paciente.getId());
        verify(logger, times(1)).info("Consulta salva com sucesso com ID: {}", consultaSalva.getId());
    }

    @Test
    void deveLancarExcecaoAoSalvarConsultaComPacienteInexistente() {
        // Arrange
        Paciente pacienteInexistente = new Paciente(99L, "Inexistente", "inexistente@teste.com", null);
        Consulta novaConsulta = new Consulta(null, pacienteInexistente, medico, null,
                LocalDateTime.now().plusDays(5), StatusConsulta.AGENDADA, "Nova consulta");

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            consultaService.salvarConsulta(novaConsulta);
        });

        assertEquals("Paciente não encontrado com ID: 99", exception.getMessage());
        verify(consultaRepository, never()).save(any(Consulta.class));
        verify(agendamentoProducer, never()).enviarMensagemConsulta(any(ConsultaNotificacaoDTO.class));
        verify(logger, times(1)).info("Tentando salvar nova consulta para o paciente ID: {}", 99L);
    }
    
    @Test
    void deveLancarExcecaoAoSalvarConsultaSemMedicoOuEnfermeiro() {
        // Arrange
        Consulta novaConsulta = new Consulta(null, paciente, null, null, // Sem médico nem enfermeiro
                LocalDateTime.now().plusDays(5), StatusConsulta.AGENDADA, "Nova consulta");

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            consultaService.salvarConsulta(novaConsulta);
        });

        assertEquals("É obrigatório associar um Médico ou um Enfermeiro à consulta.", exception.getMessage());
        verify(consultaRepository, never()).save(any(Consulta.class));
        verify(agendamentoProducer, never()).enviarMensagemConsulta(any(ConsultaNotificacaoDTO.class));
    }

    @Test
    void deveAtualizarConsultaComSucessoEEnviarMensagemKafka() {
        // Arrange
        Consulta dadosAtualizacao = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(10), StatusConsulta.AGENDADA, "Observação atualizada");
        Consulta consultaAtualizada = new Consulta(1L, paciente, medico, null,
                dadosAtualizacao.getDataHora(), StatusConsulta.AGENDADA, "Observação atualizada");

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta)); // Retorna a consulta original
        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaAtualizada);
        doNothing().when(agendamentoProducer).enviarMensagemConsulta(any(ConsultaNotificacaoDTO.class));

        // Act
        Consulta resultado = consultaService.atualizarConsulta(1L, dadosAtualizacao);

        // Assert
        assertNotNull(resultado);
        assertEquals(consultaAtualizada.getId(), resultado.getId());
        assertEquals(dadosAtualizacao.getDataHora(), resultado.getDataHora());
        assertEquals(dadosAtualizacao.getObservacoes(), resultado.getObservacoes());

        verify(consultaRepository, times(1)).findById(1L);
        verify(pacienteRepository, times(1)).findById(paciente.getId());
        verify(medicoRepository, times(1)).findById(medico.getId());
        verify(consultaRepository, times(1)).save(any(Consulta.class));

        ArgumentCaptor<ConsultaNotificacaoDTO> dtoCaptor = ArgumentCaptor.forClass(ConsultaNotificacaoDTO.class);
        verify(agendamentoProducer, times(1)).enviarMensagemConsulta(dtoCaptor.capture());
        assertEquals(consultaAtualizada.getId(), dtoCaptor.getValue().getId());
        assertEquals("ATUALIZACAO", dtoCaptor.getValue().getTipoNotificacao());

        verify(logger, times(1)).info("Tentando atualizar consulta com ID: {}", 1L);
        verify(logger, times(1)).info("Consulta com ID: {} atualizada com sucesso", 1L);
    }

    @Test
    void deveLancarExcecaoAoAtualizarConsultaInexistente() {
        // Arrange
        Consulta dadosAtualizacao = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(10), StatusConsulta.AGENDADA, "Observação atualizada");

        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            consultaService.atualizarConsulta(99L, dadosAtualizacao);
        });

        assertEquals("Consulta não encontrada com ID: 99", exception.getMessage());
        verify(consultaRepository, never()).save(any(Consulta.class));
        verify(agendamentoProducer, never()).enviarMensagemConsulta(any(ConsultaNotificacaoDTO.class));
        verify(logger, times(1)).info("Tentando atualizar consulta com ID: {}", 99L);
        verify(logger, times(1)).warn("Consulta com ID: {} não encontrada para atualização", 99L);
    }

    @Test
    void deveExcluirConsultaComSucesso() {
        // Arrange
        when(consultaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(consultaRepository).deleteById(1L);

        // Act
        assertDoesNotThrow(() -> consultaService.excluirConsulta(1L));

        // Assert
        verify(consultaRepository, times(1)).existsById(1L);
        verify(consultaRepository, times(1)).deleteById(1L);
        verify(logger, times(1)).info("Tentando excluir consulta com ID: {}", 1L);
        verify(logger, times(1)).info("Consulta com ID: {} excluída com sucesso", 1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirConsultaInexistente() {
        // Arrange
        when(consultaRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            consultaService.excluirConsulta(99L);
        });

        assertEquals("Consulta não encontrada com ID: 99", exception.getMessage());
        verify(consultaRepository, times(1)).existsById(99L);
        verify(consultaRepository, never()).deleteById(anyLong());
        verify(logger, times(1)).info("Tentando excluir consulta com ID: {}", 99L);
        verify(logger, times(1)).warn("Consulta com ID: {} não encontrada para exclusão", 99L);
    }
}

