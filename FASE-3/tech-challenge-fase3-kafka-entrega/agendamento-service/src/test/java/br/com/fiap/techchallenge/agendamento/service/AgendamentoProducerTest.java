package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.model.StatusConsulta;
import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe AgendamentoProducer.
 */
@ExtendWith(MockitoExtension.class)
class AgendamentoProducerTest {

    @Mock
    private KafkaTemplate<String, ConsultaNotificacaoDTO> kafkaTemplate;

    @Mock
    private Logger logger; // Mock do Logger para verificar logs

    @InjectMocks
    private AgendamentoProducer agendamentoProducer;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<ConsultaNotificacaoDTO> dtoCaptor;

    private final String NOME_TOPICO_TESTE = "consultas.teste.topic";

    @BeforeEach
    void setUp() {
        // Injeta o nome do tópico de teste no producer
        ReflectionTestUtils.setField(agendamentoProducer, "nomeTopicoConsulta", NOME_TOPICO_TESTE);
        // Injeta o logger mockado (opcional, mas útil para verificar logs)
        ReflectionTestUtils.setField(AgendamentoProducer.class, "logger", logger);
    }

    @Test
    void deveEnviarMensagemConsultaComSucesso() {
        // Arrange
        ConsultaNotificacaoDTO dto = new ConsultaNotificacaoDTO(
                1L, 10L, "Paciente Teste", "paciente@teste.com", "11999998888",
                LocalDateTime.now().plusDays(1), StatusConsulta.AGENDADA.name(), "CRIACAO"
        );

        // Configura o mock do KafkaTemplate para não fazer nada quando send for chamado
        // Usamos lenient() pois o logger pode ser chamado ou não dependendo do nível configurado
        lenient().when(kafkaTemplate.send(anyString(), anyString(), any(ConsultaNotificacaoDTO.class))).thenReturn(null);

        // Act
        agendamentoProducer.enviarMensagemConsulta(dto);

        // Assert
        // Verifica se o método send do KafkaTemplate foi chamado uma vez
        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), dtoCaptor.capture());

        // Verifica os argumentos passados para o método send
        assertEquals(NOME_TOPICO_TESTE, topicCaptor.getValue());
        assertEquals(String.valueOf(dto.getId()), keyCaptor.getValue()); // Verifica se a chave é o ID da consulta
        assertEquals(dto, dtoCaptor.getValue());

        // Verifica se logs informativos foram chamados (ajustar conforme implementação do log)
        verify(logger, times(1)).info(eq("Enviando mensagem para o tópico Kafka \n{}\n: \n{}\n"), eq(NOME_TOPICO_TESTE), eq(dto));
        verify(logger, times(1)).info(eq("Mensagem enviada com sucesso para o tópico \n{}\n"), eq(NOME_TOPICO_TESTE));
        verify(logger, never()).error(anyString(), any(), any(), any()); // Garante que não houve log de erro
    }

    @Test
    void deveLogarErroQuandoKafkaTemplateFalhar() {
        // Arrange
        ConsultaNotificacaoDTO dto = new ConsultaNotificacaoDTO(
                2L, 20L, "Outro Paciente", "outro@teste.com", null,
                LocalDateTime.now().plusHours(2), StatusConsulta.AGENDADA.name(), "ATUALIZACAO"
        );
        RuntimeException exceptionSimulada = new RuntimeException("Erro ao conectar ao Kafka");

        // Configura o mock do KafkaTemplate para lançar uma exceção
        when(kafkaTemplate.send(anyString(), anyString(), any(ConsultaNotificacaoDTO.class))).thenThrow(exceptionSimulada);

        // Act
        agendamentoProducer.enviarMensagemConsulta(dto);

        // Assert
        // Verifica se o método send foi chamado
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(ConsultaNotificacaoDTO.class));

        // Verifica se o erro foi logado
        verify(logger, times(1)).error(
                eq("Erro ao enviar mensagem para o tópico Kafka \n{}\n: \n{}\n"),
                eq(NOME_TOPICO_TESTE),
                eq(exceptionSimulada.getMessage()),
                eq(exceptionSimulada)
        );
        // Verifica se o log de sucesso não foi chamado
        verify(logger, never()).info(eq("Mensagem enviada com sucesso para o tópico \n{}\n"), eq(NOME_TOPICO_TESTE));
    }
}

