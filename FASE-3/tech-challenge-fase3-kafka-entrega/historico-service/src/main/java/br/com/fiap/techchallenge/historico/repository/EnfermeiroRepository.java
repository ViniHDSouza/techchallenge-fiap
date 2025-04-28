package br.com.fiap.techchallenge.historico.repository;

import br.com.fiap.techchallenge.historico.model.Enfermeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Enfermeiro no serviço de histórico.
 */
@Repository
public interface EnfermeiroRepository extends JpaRepository<Enfermeiro, Long> {
    // Métodos de consulta específicos podem ser adicionados aqui, se necessário para GraphQL
}
