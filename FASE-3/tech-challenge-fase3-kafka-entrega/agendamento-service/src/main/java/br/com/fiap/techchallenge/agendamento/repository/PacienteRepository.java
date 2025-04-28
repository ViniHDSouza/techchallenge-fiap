package br.com.fiap.techchallenge.agendamento.repository;

import br.com.fiap.techchallenge.agendamento.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Paciente.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    /**
     * Busca um paciente pelo email.
     * 
     * @param email Email do paciente a ser buscado
     * @return Paciente encontrado ou null se não existir
     */
    Paciente findByEmail(String email);
}
