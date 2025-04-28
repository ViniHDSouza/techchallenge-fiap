package br.com.fiap.techchallenge.historico.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade que representa um enfermeiro no sistema de histórico.
 * (Assume-se que os dados são sincronizados ou compartilhados com o serviço de agendamento).
 */
@Entity
@Table(name = "enfermeiros") // Mesmo nome de tabela para consistência
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enfermeiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do enfermeiro.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Número do registro no Conselho Regional de Enfermagem (COREN).
     */
    @Column(nullable = false, unique = true)
    private String coren;
}
