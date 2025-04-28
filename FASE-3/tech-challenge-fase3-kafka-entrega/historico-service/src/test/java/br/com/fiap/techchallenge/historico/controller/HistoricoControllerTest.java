package br.com.fiap.techchallenge.historico.controller;

import br.com.fiap.techchallenge.historico.model.*;
import br.com.fiap.techchallenge.historico.repository.ConsultaRepository;
import br.com.fiap.techchallenge.historico.repository.EnfermeiroRepository;
import br.com.fiap.techchallenge.historico.repository.MedicoRepository;
import br.com.fiap.techchallenge.historico.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Testes unitários para o controlador GraphQL do serviço de histórico.
 */
@GraphQlTest(HistoricoController.class)
class HistoricoControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private ConsultaRepository consultaRepository;

    @MockBean
    private PacienteRepository pacienteRepository;

    @MockBean
    private MedicoRepository medicoRepository;

    @MockBean
    private EnfermeiroRepository enfermeiroRepository;

    @Test
    void deveRetornarConsultasPorPaciente() {
        // Arrange
        Long pacienteId = 1L;
        Paciente paciente = new Paciente(pacienteId, "Paciente Teste", "paciente@teste.com", "11999998888");
        Medico medico = new Medico(1L, "Dr. Teste", "Cardiologia", "CRM12345");
        
        Consulta consulta1 = new Consulta(1L, paciente, medico, null, 
                LocalDateTime.now().plusDays(1), StatusConsulta.AGENDADA, "Observação 1");
        Consulta consulta2 = new Consulta(2L, paciente, medico, null, 
                LocalDateTime.now().plusDays(2), StatusConsulta.AGENDADA, "Observação 2");
        
        List<Consulta> consultas = Arrays.asList(consulta1, consulta2);
        
        when(consultaRepository.findByPacienteId(pacienteId)).thenReturn(consultas);

        // Act & Assert
        String query = """
                query {
                  consultasPorPaciente(pacienteId: "1") {
                    id
                    dataHora
                    status
                    observacoes
                    paciente {
                      id
                      nome
                    }
                    medico {
                      id
                      nome
                      especialidade
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("consultasPorPaciente")
                .entityList(Consulta.class)
                .hasSize(2);

        verify(consultaRepository, times(1)).findByPacienteId(pacienteId);
    }

    @Test
    void deveRetornarConsultaEspecifica() {
        // Arrange
        Long consultaId = 1L;
        Paciente paciente = new Paciente(1L, "Paciente Teste", "paciente@teste.com", "11999998888");
        Medico medico = new Medico(1L, "Dr. Teste", "Cardiologia", "CRM12345");
        
        Consulta consulta = new Consulta(consultaId, paciente, medico, null, 
                LocalDateTime.now().plusDays(1), StatusConsulta.AGENDADA, "Observação teste");
        
        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));

        // Act & Assert
        String query = """
                query {
                  consulta(id: "1") {
                    id
                    dataHora
                    status
                    observacoes
                    paciente {
                      id
                      nome
                      email
                    }
                    medico {
                      id
                      nome
                      especialidade
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("consulta")
                .entity(Consulta.class)
                .isEqualTo(consulta);

        verify(consultaRepository, times(1)).findById(consultaId);
    }

    @Test
    void deveRetornarNullQuandoConsultaNaoExiste() {
        // Arrange
        Long consultaId = 999L;
        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        // Act & Assert
        String query = """
                query {
                  consulta(id: "999") {
                    id
                    dataHora
                    status
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("consulta")
                .valueIsNull();

        verify(consultaRepository, times(1)).findById(consultaId);
    }

    @Test
    void deveRetornarListaDePacientes() {
        // Arrange
        List<Paciente> pacientes = Arrays.asList(
                new Paciente(1L, "Paciente 1", "paciente1@teste.com", "11999998881"),
                new Paciente(2L, "Paciente 2", "paciente2@teste.com", "11999998882")
        );
        
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        // Act & Assert
        String query = """
                query {
                  pacientes {
                    id
                    nome
                    email
                    telefone
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("pacientes")
                .entityList(Paciente.class)
                .hasSize(2);

        verify(pacienteRepository, times(1)).findAll();
    }
}
