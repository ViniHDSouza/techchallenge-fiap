package br.com.fiap.techchallenge.agendamento.service;

import br.com.fiap.techchallenge.agendamento.config.ConsultaNotificacaoMapper;
import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.MedicoRepository;
import br.com.fiap.techchallenge.agendamento.repository.EnfermeiroRepository;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;

import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar as operações relacionadas a Consultas.
 */
@Service
public class ConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaService.class);

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EnfermeiroRepository enfermeiroRepository;
    private final AgendamentoProducer agendamentoProducer; // Produtor Kafka

    @Autowired
    public ConsultaService(ConsultaRepository consultaRepository,
                           PacienteRepository pacienteRepository,
                           MedicoRepository medicoRepository,
                           EnfermeiroRepository enfermeiroRepository,
                           AgendamentoProducer agendamentoProducer) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.enfermeiroRepository = enfermeiroRepository;
        this.agendamentoProducer = agendamentoProducer;
    }

    /**
     * Lista todas as consultas cadastradas.
     *
     * @return Lista de todas as consultas.
     */
    public List<Consulta> listarTodasConsultas() {
        logger.debug("Listando todas as consultas");
        return consultaRepository.findAll();
    }

    /**
     * Busca uma consulta pelo seu ID.
     *
     * @param id ID da consulta.
     * @return Optional contendo a consulta, se encontrada.
     */
    public Optional<Consulta> buscarConsultaPorId(Long id) {
        logger.debug("Buscando consulta com ID: {}", id);
        return consultaRepository.findById(id);
    }

    /**
     * Busca todas as consultas de um paciente específico.
     *
     * @param pacienteId ID do paciente.
     * @return Lista de consultas do paciente.
     */
    public List<Consulta> buscarConsultasPorPaciente(Long pacienteId) {
        logger.debug("Buscando consultas para o paciente com ID: {}", pacienteId);
        return consultaRepository.findByPacienteId(pacienteId);
    }

    /**
     * Busca todas as consultas futuras de um paciente específico.
     *
     * @param pacienteId ID do paciente.
     * @return Lista de consultas futuras do paciente.
     */
    public List<Consulta> buscarConsultasFuturasPorPaciente(Long pacienteId) {
        LocalDateTime agora = LocalDateTime.now();
        logger.debug("Buscando consultas futuras para o paciente com ID: {} a partir de {}", pacienteId, agora);
        return consultaRepository.findConsultasFuturasByPacienteId(pacienteId, agora);
    }

    /**
     * Salva uma nova consulta no banco de dados e envia uma notificação via Kafka.
     *
     * @param consulta Consulta a ser salva.
     * @return A consulta salva.
     */
    @Transactional
    public Consulta salvarConsulta(Consulta consulta) {
        logger.info("Tentando salvar nova consulta para o paciente ID: {}", consulta.getPaciente().getId());
        
        // Valida e busca as entidades relacionadas (Paciente, Medico, Enfermeiro)
        validarEntidadesRelacionadas(consulta);

        Consulta consultaSalva = consultaRepository.save(consulta);
        logger.info("Consulta salva com sucesso com ID: {}", consultaSalva.getId());

        // Enviar mensagem para o Kafka
        ConsultaNotificacaoDTO notificacaoDTO = ConsultaNotificacaoMapper.fromEntity(consultaSalva, "CRIACAO");
        agendamentoProducer.enviarMensagemConsulta(notificacaoDTO);

        return consultaSalva;
    }

    /**
     * Atualiza uma consulta existente e envia uma notificação via Kafka.
     *
     * @param id       ID da consulta a ser atualizada.
     * @param consulta Dados da consulta para atualização.
     * @return A consulta atualizada.
     * @throws EntityNotFoundException se a consulta não for encontrada.
     */
    @Transactional
    public Consulta atualizarConsulta(Long id, Consulta consulta) {
        logger.info("Tentando atualizar consulta com ID: {}", id);
        return consultaRepository.findById(id).map(consultaExistente -> {
            
            // Valida e busca as entidades relacionadas (Paciente, Medico, Enfermeiro)
            validarEntidadesRelacionadas(consulta);

            // Atualiza os campos da consulta existente
            consultaExistente.setPaciente(consulta.getPaciente());
            consultaExistente.setMedico(consulta.getMedico());
            consultaExistente.setEnfermeiro(consulta.getEnfermeiro());
            consultaExistente.setDataHora(consulta.getDataHora());
            consultaExistente.setStatus(consulta.getStatus());
            consultaExistente.setObservacoes(consulta.getObservacoes());

            Consulta consultaAtualizada = consultaRepository.save(consultaExistente);
            logger.info("Consulta com ID: {} atualizada com sucesso", id);

            // Enviar mensagem para o Kafka
            ConsultaNotificacaoDTO notificacaoDTO = ConsultaNotificacaoMapper.fromEntity(consultaAtualizada, "ATUALIZACAO");
            agendamentoProducer.enviarMensagemConsulta(notificacaoDTO);

            return consultaAtualizada;
        }).orElseThrow(() -> {
            logger.warn("Consulta com ID: {} não encontrada para atualização", id);
            return new EntityNotFoundException("Consulta não encontrada com ID: " + id);
        });
    }

    /**
     * Exclui uma consulta pelo seu ID.
     *
     * @param id ID da consulta a ser excluída.
     * @throws EntityNotFoundException se a consulta não for encontrada.
     */
    @Transactional
    public void excluirConsulta(Long id) {
        logger.info("Tentando excluir consulta com ID: {}", id);
        if (consultaRepository.existsById(id)) {
            consultaRepository.deleteById(id);
            logger.info("Consulta com ID: {} excluída com sucesso", id);
            // Poderia enviar uma mensagem Kafka de cancelamento/exclusão aqui, se necessário
        } else {
            logger.warn("Consulta com ID: {} não encontrada para exclusão", id);
            throw new EntityNotFoundException("Consulta não encontrada com ID: " + id);
        }
    }

    /**
     * Valida e busca as entidades relacionadas (Paciente, Medico, Enfermeiro) 
     * referenciadas na consulta, garantindo que existam no banco de dados.
     *
     * @param consulta Consulta contendo as referências.
     * @throws EntityNotFoundException se alguma entidade relacionada não for encontrada.
     */
    private void validarEntidadesRelacionadas(Consulta consulta) {
        // Valida Paciente
        if (consulta.getPaciente() == null || consulta.getPaciente().getId() == null) {
            throw new IllegalArgumentException("ID do Paciente é obrigatório na consulta.");
        }
        consulta.setPaciente(pacienteRepository.findById(consulta.getPaciente().getId())
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado com ID: " + consulta.getPaciente().getId())));

        // Valida Medico (se informado)
        if (consulta.getMedico() != null && consulta.getMedico().getId() != null) {
            consulta.setMedico(medicoRepository.findById(consulta.getMedico().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Médico não encontrado com ID: " + consulta.getMedico().getId())));
        } else {
            consulta.setMedico(null); // Garante que está nulo se não informado ID
        }

        // Valida Enfermeiro (se informado)
        if (consulta.getEnfermeiro() != null && consulta.getEnfermeiro().getId() != null) {
            consulta.setEnfermeiro(enfermeiroRepository.findById(consulta.getEnfermeiro().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Enfermeiro não encontrado com ID: " + consulta.getEnfermeiro().getId())));
        } else {
            consulta.setEnfermeiro(null); // Garante que está nulo se não informado ID
        }

        // Validação adicional: garantir que pelo menos um profissional (médico ou enfermeiro) foi associado
        if (consulta.getMedico() == null && consulta.getEnfermeiro() == null) {
            throw new IllegalArgumentException("É obrigatório associar um Médico ou um Enfermeiro à consulta.");
        }
    }
}

