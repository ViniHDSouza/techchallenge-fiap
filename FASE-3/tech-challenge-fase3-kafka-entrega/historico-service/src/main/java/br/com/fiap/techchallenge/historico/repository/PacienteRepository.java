package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Paciente no serviço de histórico.
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Métodos de consulta específicos podem ser adicionados aqui, se necessário para GraphQL
}
