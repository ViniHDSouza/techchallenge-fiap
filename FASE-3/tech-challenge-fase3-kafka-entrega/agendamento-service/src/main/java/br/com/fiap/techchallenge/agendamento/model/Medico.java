package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade que representa um médico no sistema.
 */
@Entity
@Table(name = "medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do médico.
     * Campo obrigatório.
     */
    @NotBlank(message = "Nome do médico é obrigatório")
    @Column(nullable = false)
    private String nome;

    /**
     * Especialidade médica.
     * Campo opcional.
     */
    private String especialidade;

    /**
     * Número do registro no Conselho Regional de Medicina (CRM).
     * Campo obrigatório e único.
     */
    @NotBlank(message = "CRM do médico é obrigatório")
    @Column(nullable = false, unique = true)
    private String crm;
}

