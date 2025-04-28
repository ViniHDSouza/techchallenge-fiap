package br.com.fiap.techchallenge.historico.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade que representa um paciente no sistema de histórico.
 * (Assume-se que os dados são sincronizados ou compartilhados com o serviço de agendamento).
 */
@Entity
@Table(name = "pacientes") // Mesmo nome de tabela para consistência
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do paciente.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Email do paciente.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Número de telefone do paciente.
     */
    private String telefone;
}
