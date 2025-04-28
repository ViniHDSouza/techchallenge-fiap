package br.com.fiap.techchallenge.agendamento.controller;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import br.com.fiap.techchallenge.agendamento.service.ConsultaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar as operações de Consulta.
 */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaController.class);

    private final ConsultaService consultaService;

    @Autowired
    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /**
     * Endpoint para listar todas as consultas.
     *
     * @return ResponseEntity com a lista de consultas e status OK.
     */
    @GetMapping
    public ResponseEntity<List<Consulta>> listarTodasConsultas() {
        logger.info("Recebida requisição para listar todas as consultas");
        List<Consulta> consultas = consultaService.listarTodasConsultas();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para buscar uma consulta pelo ID.
     *
     * @param id ID da consulta.
     * @return ResponseEntity com a consulta encontrada e status OK, ou status NOT_FOUND.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Consulta> buscarConsultaPorId(@PathVariable Long id) {
        logger.info("Recebida requisição para buscar consulta com ID: {}", id);
        Optional<Consulta> consulta = consultaService.buscarConsultaPorId(id);
        return consulta.map(ResponseEntity::ok)
                       .orElseGet(() -> {
                           logger.warn("Consulta com ID: {} não encontrada", id);
                           return ResponseEntity.notFound().build();
                       });
    }

    /**
     * Endpoint para buscar todas as consultas de um paciente específico.
     *
     * @param pacienteId ID do paciente.
     * @return ResponseEntity com a lista de consultas do paciente e status OK.
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Consulta>> buscarConsultasPorPaciente(@PathVariable Long pacienteId) {
        logger.info("Recebida requisição para buscar consultas do paciente com ID: {}", pacienteId);
        List<Consulta> consultas = consultaService.buscarConsultasPorPaciente(pacienteId);
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para buscar as consultas futuras de um paciente específico.
     *
     * @param pacienteId ID do paciente.
     * @return ResponseEntity com a lista de consultas futuras do paciente e status OK.
     */
    @GetMapping("/paciente/{pacienteId}/futuras")
    public ResponseEntity<List<Consulta>> buscarConsultasFuturasPorPaciente(@PathVariable Long pacienteId) {
        logger.info("Recebida requisição para buscar consultas futuras do paciente com ID: {}", pacienteId);
        List<Consulta> consultas = consultaService.buscarConsultasFuturasPorPaciente(pacienteId);
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para criar uma nova consulta.
     *
     * @param consulta Objeto Consulta a ser criado (validado).
     * @return ResponseEntity com a consulta criada e status CREATED.
     */
    @PostMapping
    public ResponseEntity<Consulta> criarConsulta(@Valid @RequestBody Consulta consulta) {
        logger.info("Recebida requisição para criar nova consulta");
        try {
            // A validação das entidades relacionadas (Paciente, Medico, Enfermeiro) é feita no ConsultaService
            Consulta novaConsulta = consultaService.salvarConsulta(consulta);
            logger.info("Nova consulta criada com ID: {}", novaConsulta.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(novaConsulta);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Erro ao criar consulta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null); // Retorna 400 Bad Request com corpo vazio
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar consulta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para atualizar uma consulta existente.
     *
     * @param id       ID da consulta a ser atualizada.
     * @param consulta Objeto Consulta com os dados atualizados (validado).
     * @return ResponseEntity com a consulta atualizada e status OK, ou status NOT_FOUND/BAD_REQUEST.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Consulta> atualizarConsulta(@PathVariable Long id, @RequestBody Consulta consulta) {
        logger.info("Recebida requisição para atualizar consulta com ID: {}", id);
        try {
            Consulta consultaAtualizada = consultaService.atualizarConsulta(id, consulta);
            logger.info("Consulta com ID: {} atualizada com sucesso", id);
            return ResponseEntity.ok(consultaAtualizada);
        } catch (EntityNotFoundException e) {
            logger.warn("Erro ao atualizar consulta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar consulta com ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar consulta com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para excluir uma consulta.
     *
     * @param id ID da consulta a ser excluída.
     * @return ResponseEntity com status NO_CONTENT se excluído, ou status NOT_FOUND.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConsulta(@PathVariable Long id) {
        logger.info("Recebida requisição para excluir consulta com ID: {}", id);
        try {
            consultaService.excluirConsulta(id);
            logger.info("Consulta com ID: {} excluída com sucesso", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Erro ao excluir consulta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir consulta com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

