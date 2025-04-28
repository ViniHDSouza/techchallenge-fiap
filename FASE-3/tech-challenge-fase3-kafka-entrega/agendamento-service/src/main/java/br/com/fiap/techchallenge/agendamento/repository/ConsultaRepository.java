package br.com.fiap.techchallenge.agendamento.repository;

import br.com.fiap.techchallenge.agendamento.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para operações de persistência da entidade Consulta.
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Busca todas as consultas associadas a um paciente específico.
     *
     * @param pacienteId ID do paciente
     * @return Lista de consultas do paciente
     */
    List<Consulta> findByPacienteId(Long pacienteId);

    /**
     * Busca todas as consultas futuras (data e hora maior que a atual) associadas a um paciente específico.
     *
     * @param pacienteId ID do paciente
     * @param agora      Data e hora atual para comparação
     * @return Lista de consultas futuras do paciente
     */
    @Query("SELECT c FROM Consulta c WHERE c.paciente.id = :pacienteId AND c.dataHora > :agora")
    List<Consulta> findConsultasFuturasByPacienteId(@Param("pacienteId") Long pacienteId, @Param("agora") LocalDateTime agora);

    /**
     * Busca todas as consultas associadas a um médico específico.
     *
     * @param medicoId ID do médico
     * @return Lista de consultas do médico
     */
    List<Consulta> findByMedicoId(Long medicoId);

    /**
     * Busca todas as consultas associadas a um enfermeiro específico.
     *
     * @param enfermeiroId ID do enfermeiro
     * @return Lista de consultas do enfermeiro
     */
    List<Consulta> findByEnfermeiroId(Long enfermeiroId);
}

