package br.com.fiap.techchallenge.notificacao.service;

import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe NotificacaoConsumer.
 */
@ExtendWith(MockitoExtension.class)
class NotificacaoConsumerTest {

    @Mock
    private Logger logger; // Mock do Logger para verificar logs

    @InjectMocks
    private NotificacaoConsumer notificacaoConsumer;

    @BeforeEach
    void setUp() {
        // Injeta o logger mockado
        ReflectionTestUtils.setField(NotificacaoConsumer.class, "logger", logger);
    }

    @Test
    void deveProcessarMensagemCriacaoComSucesso() {
        // Arrange
        ConsultaNotificacaoDTO dto = new ConsultaNotificacaoDTO(
                1L, 10L, "Paciente Teste", "paciente@teste.com", "11999998888",
                LocalDateTime.now().plusDays(1), "AGENDADA", "CRIACAO"
        );

        // Act
        notificacaoConsumer.consumirMensagemConsulta(dto);

        // Assert
        // Verifica se os logs foram chamados corretamente
        verify(logger, times(1)).info(contains("Mensagem recebida do Kafka no tópico"), eq("consultas.topic"), eq(dto));
        verify(logger, times(1)).info(eq("--- SIMULANDO ENVIO DE NOTIFICAÇÃO ---"));
        verify(logger, times(1)).info(contains("Para:"), contains(dto.getPacienteNome()), contains(dto.getPacienteEmail()));
        verify(logger, times(1)).info(contains("Mensagem:"), contains("foi agendada com sucesso"));
        verify(logger, times(1)).info(eq("--- FIM DA SIMULAÇÃO ---"));
        verify(logger, times(1)).info(contains("Notificação processada com sucesso"), contains(dto.getId().toString()));
        verify(logger, never()).error(anyString(), any(), any(), any()); // Garante que não houve log de erro
    }

    @Test
    void deveProcessarMensagemAtualizacaoComSucesso() {
        // Arrange
        ConsultaNotificacaoDTO dto = new ConsultaNotificacaoDTO(
                2L, 20L, "Outro Paciente", "outro@teste.com", null,
                LocalDateTime.now().plusHours(2), "AGENDADA", "ATUALIZACAO"
        );

        // Act
        notificacaoConsumer.consumirMensagemConsulta(dto);

        // Assert
        // Verifica se os logs foram chamados corretamente
        verify(logger, times(1)).info(contains("Mensagem recebida do Kafka no tópico"), eq("consultas.topic"), eq(dto));
        verify(logger, times(1)).info(eq("--- SIMULANDO ENVIO DE NOTIFICAÇÃO ---"));
        verify(logger, times(1)).info(contains("Para:"), contains(dto.getPacienteNome()), contains(dto.getPacienteEmail()));
        verify(logger, times(1)).info(contains("Mensagem:"), contains("foi remarcada para"));
        verify(logger, times(1)).info(eq("--- FIM DA SIMULAÇÃO ---"));
        verify(logger, times(1)).info(contains("Notificação processada com sucesso"), contains(dto.getId().toString()));
        verify(logger, never()).error(anyString(), any(), any(), any()); // Garante que não houve log de erro
    }

    @Test
    void deveLogarErroQuandoProcessamentoFalhar() {
        // Arrange
        ConsultaNotificacaoDTO dto = new ConsultaNotificacaoDTO(
                3L, 30L, null, "email@teste.com", "11999998888",
                LocalDateTime.now().plusDays(1), "AGENDADA", "CRIACAO"
        );

        // Configura o mock do logger para lançar uma exceção quando info for chamado
        doThrow(new RuntimeException("Erro simulado")).when(logger).info(eq("--- SIMULANDO ENVIO DE NOTIFICAÇÃO ---"));

        // Act
        notificacaoConsumer.consumirMensagemConsulta(dto);

        // Assert
        // Verifica se o erro foi logado
        verify(logger, times(1)).error(contains("Erro ao processar mensagem do Kafka"), eq(dto.getId()), contains("Erro simulado"), any(RuntimeException.class));
    }
}
