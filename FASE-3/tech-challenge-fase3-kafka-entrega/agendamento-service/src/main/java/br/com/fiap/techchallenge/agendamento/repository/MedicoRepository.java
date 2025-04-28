package br.com.fiap.techchallenge.agendamento.repository;

import br.com.fiap.techchallenge.agendamento.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para operações de persistência da entidade Medico.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /**
     * Busca um médico pelo CRM.
     *
     * @param crm Número do CRM do médico a ser buscado
     * @return Medico encontrado ou null se não existir
     */
    Medico findByCrm(String crm);
}

