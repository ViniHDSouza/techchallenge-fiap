package br.com.fiap.techchallenge.historico.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma consulta no sistema de histórico.
 * (Assume-se que os dados são sincronizados ou compartilhados com o serviço de agendamento).
 */
@Entity
@Table(name = "consultas") // Mesmo nome de tabela para consistência
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Paciente associado à consulta.
     */
    @ManyToOne(fetch = FetchType.EAGER)
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
     */
    @Column(nullable = false)
    private LocalDateTime dataHora;

    /**
     * Status atual da consulta (ex: AGENDADA, REALIZADA, CANCELADA).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    /**
     * Observações adicionais sobre a consulta.
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;
}
