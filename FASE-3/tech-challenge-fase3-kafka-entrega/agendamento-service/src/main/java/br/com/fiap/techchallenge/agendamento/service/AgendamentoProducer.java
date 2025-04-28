package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por enviar mensagens de notificação de consulta para o Kafka.
 */
@Service
public class AgendamentoProducer {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoProducer.class);

    // Injeta o KafkaTemplate configurado pelo Spring Boot
    private final KafkaTemplate<String, ConsultaNotificacaoDTO> kafkaTemplate;

    // Injeta o nome do tópico definido em application.properties
    @Value("${kafka.topic.consulta.nome}")
    private String nomeTopicoConsulta;

    @Autowired
    public AgendamentoProducer(KafkaTemplate<String, ConsultaNotificacaoDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envia uma mensagem de notificação de consulta para o tópico Kafka.
     *
     * @param dto DTO contendo as informações da consulta para notificação.
     */
    public void enviarMensagemConsulta(ConsultaNotificacaoDTO dto) {
        try {
            logger.info("Enviando mensagem para o topico Kafka {}:{}", nomeTopicoConsulta, dto);
            // Envia a mensagem usando o ID da consulta como chave (para garantir ordenação por consulta, se necessário)
            kafkaTemplate.send(nomeTopicoConsulta, String.valueOf(dto.getId()), dto);
            logger.info("Mensagem enviada com sucesso para o topico {}", nomeTopicoConsulta);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para o topico Kafka {}: {}", nomeTopicoConsulta, e.getMessage(), e);
            // Implementar lógica de tratamento de erro (ex: retry, DLQ) se necessário
        }
    }
}

