package br.com.fiap.techchallenge.historico.controller;

import br.com.fiap.techchallenge.historico.model.*;
import br.com.fiap.techchallenge.historico.repository.ConsultaRepository;
import br.com.fiap.techchallenge.historico.repository.EnfermeiroRepository;
import br.com.fiap.techchallenge.historico.repository.MedicoRepository;
import br.com.fiap.techchallenge.historico.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador GraphQL que expõe as queries definidas no schema.
 */
@Controller
public class HistoricoController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoController.class);

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EnfermeiroRepository enfermeiroRepository;

    @Autowired
    public HistoricoController(ConsultaRepository consultaRepository,
                               PacienteRepository pacienteRepository,
                               MedicoRepository medicoRepository,
                               EnfermeiroRepository enfermeiroRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.enfermeiroRepository = enfermeiroRepository;
    }

    /**
     * Resolve a query GraphQL 'consultasPorPaciente'.
     *
     * @param pacienteId ID do paciente.
     * @return Lista de consultas do paciente.
     */
    @QueryMapping
    public List<Consulta> consultasPorPaciente(@Argument Long pacienteId) {
        logger.debug("Recebida query GraphQL 'consultasPorPaciente' para o ID: {}", pacienteId);
        return consultaRepository.findByPacienteId(pacienteId);
    }

    /**
     * Resolve a query GraphQL 'consultasFuturasPorPaciente'.
     *
     * @param pacienteId ID do paciente.
     * @return Lista de consultas futuras do paciente.
     */
    @QueryMapping
    public List<Consulta> consultasFuturasPorPaciente(@Argument Long pacienteId) {
        LocalDateTime agora = LocalDateTime.now();
        logger.debug("Recebida query GraphQL 'consultasFuturasPorPaciente' para o ID: {} a partir de {}", pacienteId, agora);
        return consultaRepository.findConsultasFuturasByPacienteId(pacienteId, agora);
    }

    /**
     * Resolve a query GraphQL 'consultasPorMedico'.
     *
     * @param medicoId ID do médico.
     * @return Lista de consultas do médico.
     */
    @QueryMapping
    public List<Consulta> consultasPorMedico(@Argument Long medicoId) {
        logger.debug("Recebida query GraphQL 'consultasPorMedico' para o ID: {}", medicoId);
        return consultaRepository.findByMedicoId(medicoId);
    }

    /**
     * Resolve a query GraphQL 'consultasPorEnfermeiro'.
     *
     * @param enfermeiroId ID do enfermeiro.
     * @return Lista de consultas do enfermeiro.
     */
    @QueryMapping
    public List<Consulta> consultasPorEnfermeiro(@Argument Long enfermeiroId) {
        logger.debug("Recebida query GraphQL 'consultasPorEnfermeiro' para o ID: {}", enfermeiroId);
        return consultaRepository.findByEnfermeiroId(enfermeiroId);
    }

    /**
     * Resolve a query GraphQL 'consulta'.
     *
     * @param id ID da consulta.
     * @return A consulta encontrada ou null.
     */
    @QueryMapping
    public Consulta consulta(@Argument Long id) {
        logger.debug("Recebida query GraphQL 'consulta' para o ID: {}", id);
        Optional<Consulta> consulta = consultaRepository.findById(id);
        if (consulta.isEmpty()) {
            logger.warn("Consulta com ID: {} não encontrada via GraphQL", id);
        }
        return consulta.orElse(null); // GraphQL lida bem com retornos nulos
    }

    /**
     * Resolve a query GraphQL 'pacientes'.
     *
     * @return Lista de todos os pacientes.
     */
    @QueryMapping
    public List<Paciente> pacientes() {
        logger.debug("Recebida query GraphQL 'pacientes'");
        return pacienteRepository.findAll();
    }

    /**
     * Resolve a query GraphQL 'medicos'.
     *
     * @return Lista de todos os médicos.
     */
    @QueryMapping
    public List<Medico> medicos() {
        logger.debug("Recebida query GraphQL 'medicos'");
        return medicoRepository.findAll();
    }

    /**
     * Resolve a query GraphQL 'enfermeiros'.
     *
     * @return Lista de todos os enfermeiros.
     */
    @QueryMapping
    public List<Enfermeiro> enfermeiros() {
        logger.debug("Recebida query GraphQL 'enfermeiros'");
        return enfermeiroRepository.findAll();
    }
}

