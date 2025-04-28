package br.com.fiap.techchallenge.historico.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade que representa um médico no sistema de histórico.
 * (Assume-se que os dados são sincronizados ou compartilhados com o serviço de agendamento).
 */
@Entity
@Table(name = "medicos") // Mesmo nome de tabela para consistência
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do médico.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Especialidade médica.
     */
    private String especialidade;

    /**
     * Número do registro no Conselho Regional de Medicina (CRM).
     */
    @Column(nullable = false, unique = true)
    private String crm;
}
