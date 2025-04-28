package br.com.fiap.techchallenge.agendamento;

import br.com.fiap.techchallenge.agendamento.model.*;
import br.com.fiap.techchallenge.agendamento.repository.ConsultaRepository;
import br.com.fiap.techchallenge.agendamento.repository.MedicoRepository;
import br.com.fiap.techchallenge.agendamento.repository.PacienteRepository;
import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o AgendamentoServiceApplication.
 * Utiliza um banco de dados H2 em memória e um broker Kafka embarcado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext // Garante que o contexto do Spring e o Kafka embarcado sejam reiniciados para cada teste
@ActiveProfiles("test") // Usar um perfil de teste se houver configurações específicas (ex: application-test.properties)
class AgendamentoServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${kafka.topic.consulta.nome}")
    private String nomeTopicoConsulta;

    private ObjectMapper objectMapper;

    private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;
    private KafkaMessageListenerContainer<String, String> container;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Limpa os repositórios antes de cada teste
        consultaRepository.deleteAll();
        pacienteRepository.deleteAll();
        medicoRepository.deleteAll();

        // Configura um consumidor Kafka para o tópico de teste
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(nomeTopicoConsulta);
        container = new KafkaMessageListenerContainer<>(cf, containerProperties);
        consumerRecords = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) record -> consumerRecords.add(record));
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    void deveCriarConsultaEEnviarMensagemKafka() throws Exception {
        // Arrange: Cria Paciente e Médico no banco H2
        Paciente paciente = pacienteRepository.save(new Paciente(null, "Paciente Kafka Test", "kafka@teste.com", "11999997777"));
        Medico medico = medicoRepository.save(new Medico(null, "Dr. Kafka", "Kafkaologia", "CRMKA987"));

        Consulta novaConsultaInput = new Consulta(null, paciente, medico, null,
                LocalDateTime.now().plusDays(3), StatusConsulta.AGENDADA, "Teste integração Kafka");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(novaConsultaInput), headers);

        // Act: Chama o endpoint POST /consultas
        ResponseEntity<Consulta> response = restTemplate.postForEntity("/consultas", request, Consulta.class);

        // Assert: Verifica a resposta HTTP
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        Long consultaCriadaId = response.getBody().getId();
        assertNotNull(consultaCriadaId);
        assertEquals(StatusConsulta.AGENDADA, response.getBody().getStatus());

        // Assert: Verifica se a consulta foi salva no banco
        Optional<Consulta> consultaSalvaOpt = consultaRepository.findById(consultaCriadaId);
        assertTrue(consultaSalvaOpt.isPresent());
        assertEquals(paciente.getId(), consultaSalvaOpt.get().getPaciente().getId());
        assertEquals(medico.getId(), consultaSalvaOpt.get().getMedico().getId());

        // Assert: Verifica se a mensagem foi enviada para o Kafka
        ConsumerRecord<String, String> received = consumerRecords.poll(10, TimeUnit.SECONDS); // Espera até 10 segundos
        assertNotNull(received, "Mensagem não recebida do Kafka");

        // Desserializa a mensagem Kafka recebida (como String)
        ConsultaNotificacaoDTO notificacaoDTO = objectMapper.readValue(received.value(), ConsultaNotificacaoDTO.class);

        // Verifica o conteúdo da mensagem Kafka
        assertEquals(consultaCriadaId, notificacaoDTO.getId());
        assertEquals(paciente.getId(), notificacaoDTO.getPacienteId());
        assertEquals(paciente.getNome(), notificacaoDTO.getPacienteNome());
        assertEquals(paciente.getEmail(), notificacaoDTO.getPacienteEmail());
        assertEquals(StatusConsulta.AGENDADA.name(), notificacaoDTO.getStatus()); // Comparando como String
        assertEquals("CRIACAO", notificacaoDTO.getTipoNotificacao());
        assertEquals(String.valueOf(consultaCriadaId), received.key()); // Verifica a chave da mensagem
    }

    // Adicionar mais testes de integração (GET, PUT, DELETE, cenários de erro, etc.)
}
