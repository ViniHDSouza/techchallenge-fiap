package br.com.fiap.techchallenge.agendamento.repository;

import br.com.fiap.techchallenge.agendamento.model.Enfermeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Enfermeiro.
 */
@Repository
public interface EnfermeiroRepository extends JpaRepository<Enfermeiro, Long> {

    /**
     * Busca um enfermeiro pelo COREN.
     *
     * @param coren Número do COREN do enfermeiro a ser buscado
     * @return Enfermeiro encontrado ou null se não existir
     */
    Enfermeiro findByCoren(String coren);
}

