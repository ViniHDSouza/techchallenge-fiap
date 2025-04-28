package br.com.fiap.techchallenge.notificacao.config;




import br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaCustomConfig {

    @Bean
    public ConsumerFactory<String, ConsultaNotificacaoDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "kafka:9092"); // Usar o nome do serviço Docker
        props.put("group.id", "notificacao-group");
        props.put("key.deserializer", ErrorHandlingDeserializer.class);
        props.put("value.deserializer", ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "br.com.fiap.techchallenge.dto.ConsultaNotificacaoDTO");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Permitir todos os pacotes (para testes)
        props.put("spring.json.trusted.packages", "*");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConsultaNotificacaoDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ConsultaNotificacaoDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}