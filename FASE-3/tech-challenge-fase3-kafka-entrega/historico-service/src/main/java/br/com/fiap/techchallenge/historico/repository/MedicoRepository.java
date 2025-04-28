package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Medico no serviço de histórico.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    // Métodos de consulta específicos podem ser adicionados aqui, se necessário para GraphQL
}
