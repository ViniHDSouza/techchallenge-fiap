package br.com.fiap.techchallenge.notificacao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;

/**
 * Serviço consumidor que escuta mensagens do Kafka sobre consultas e simula o envio de notificações.
 */
@Service
public class NotificacaoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoConsumer.class);

    /**
     * Método que escuta o tópico Kafka de consultas.
     * 
     * @param dto DTO da consulta recebido via Kafka.
     */
    @KafkaListener(topics = "${kafka.topic.consulta.nome}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirMensagemConsulta(ConsultaNotificacaoDTO dto) {
        logger.info("Mensagem recebida do Kafka no topico {}:{}", "consultas.topic", dto);

        // Simula o processamento e envio da notificação
        try {
            // Lógica para determinar o tipo de notificação (criação, atualização, lembrete, etc.)
            String tipoMensagem;
            if ("CRIACAO".equalsIgnoreCase(dto.getTipoNotificacao())) {
                tipoMensagem = String.format(
                    "Olá %s, sua consulta para %s foi agendada com sucesso!",
                    dto.getPacienteNome(),
                    dto.getDataHora().toString()
                );
            } else if ("ATUALIZACAO".equalsIgnoreCase(dto.getTipoNotificacao())) {
                tipoMensagem = String.format(
                    "Olá %s, sua consulta foi remarcada para %s.",
                    dto.getPacienteNome(),
                    dto.getDataHora().toString()
                );
            } else {
                // Poderia adicionar lógica para lembretes baseados na dataHora
                tipoMensagem = String.format(
                    "Notificação sobre consulta ID %d para %s em %s.",
                    dto.getId(),
                    dto.getPacienteNome(),
                    dto.getDataHora().toString()
                );
            }

            // Simula o envio da notificação (ex: email, SMS)
            logger.info("--- SIMULANDO ENVIO DE NOTIFICAÇÃO ---");
            logger.info("Para: {} ({})", dto.getPacienteNome(), dto.getPacienteEmail() != null ? dto.getPacienteEmail() : dto.getPacienteTelefone());
            logger.info("Mensagem: {}", tipoMensagem);
            logger.info("--- FIM DA SIMULAÇÃO ---");

            logger.info("Notificação processada com sucesso para consulta ID: {}", dto.getId());

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem do Kafka para consulta ID {}: {}", dto.getId(), e.getMessage(), e);
            // Implementar lógica de tratamento de erro (ex: mover para DLQ) se necessário
        }
    }
}

