package br.com.fiap.techchallenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para envio de mensagens de notificação de consulta via Kafka.
 * Contém apenas as informações necessárias para o serviço de notificação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaNotificacaoDTO {

    private Long id;
    private Long pacienteId;
    private String pacienteNome;
    private String pacienteEmail;
    private String pacienteTelefone;
    private LocalDateTime dataHora;
    private String status;
    private String tipoNotificacao; // "CRIACAO" ou "ATUALIZACAO"

}
