package br.com.fiap.techchallenge.agendamento.config;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;


public class ConsultaNotificacaoMapper {

    public static ConsultaNotificacaoDTO fromEntity(Consulta consulta, String tipoNotificacao) {
        return new ConsultaNotificacaoDTO(
                consulta.getId(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getNome(),
                consulta.getPaciente().getEmail(),
                consulta.getPaciente().getTelefone(),
                consulta.getDataHora(),
                consulta.getStatus().name(), // Convertendo enum para String
                tipoNotificacao
        );
    }
}