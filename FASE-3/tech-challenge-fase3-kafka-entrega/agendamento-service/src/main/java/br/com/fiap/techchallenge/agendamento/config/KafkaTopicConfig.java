package br.com.fiap.techchallenge.agendamento.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuração para criação automática de tópicos Kafka.
 */
@Configuration
public class KafkaTopicConfig {

    // Injeta o nome do tópico definido em application.properties
    @Value("${kafka.topic.consulta.nome}")
    private String nomeTopicoConsulta;

    /**
     * Cria o bean do tópico de consultas.
     * O Spring Kafka Admin criará este tópico automaticamente se ele não existir.
     * 
     * @return Bean NewTopic configurado.
     */
    @Bean
    public NewTopic consultaTopic() {
        // Define o tópico com nome, partições e réplicas (ajustar conforme necessidade)
        return TopicBuilder.name(nomeTopicoConsulta)
                .partitions(1) // Número de partições (pode ser aumentado para paralelismo)
                .replicas(1)   // Fator de replicação (deve ser <= número de brokers no cluster)
                .build();
    }
}

