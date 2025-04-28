package br.com.fiap.techchallenge.agendamento.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidade que representa um paciente no sistema.
 */
@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do paciente.
     * Campo obrigatório.
     */
    @NotBlank(message = "Nome do paciente é obrigatório")
    @Column(nullable = false)
    private String nome;

    /**
     * Email do paciente.
     * Campo obrigatório e deve ser um email válido.
     */
    @NotBlank(message = "Email do paciente é obrigatório")
    @Email(message = "Email inválido")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Número de telefone do paciente.
     * Campo opcional.
     */
    private String telefone;
}
