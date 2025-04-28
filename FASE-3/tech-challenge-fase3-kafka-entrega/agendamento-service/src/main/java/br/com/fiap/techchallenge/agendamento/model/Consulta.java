package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma consulta agendada no sistema.
 */
@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Paciente associado à consulta.
     * Campo obrigatório.
     */
    @NotNull(message = "Paciente é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER) // Eager para simplificar, pode ser Lazy em produção
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /**
     * Médico associado à consulta (opcional, pode ser um enfermeiro).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    /**
     * Enfermeiro associado à consulta (opcional, pode ser um médico).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enfermeiro_id")
    private Enfermeiro enfermeiro;

    /**
     * Data e hora da consulta.
     * Campo obrigatório e deve ser uma data futura.
     */
    @NotNull(message = "Data e hora da consulta são obrigatórias")
    @Future(message = "A data da consulta deve ser no futuro")
    @Column(nullable = false)
    private LocalDateTime dataHora;

    /**
     * Status atual da consulta (ex: AGENDADA, REALIZADA, CANCELADA).
     * Campo obrigatório.
     */
    //@NotNull(message = "Status da consulta é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    /**
     * Observações adicionais sobre a consulta.
     * Campo opcional.
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Validação para garantir que ou médico ou enfermeiro seja informado (pode ser feita no Service)
}
