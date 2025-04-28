package br.com.fiap.techchallenge.historico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal para inicialização do Microserviço de Histórico.
 */
@SpringBootApplication
public class HistoricoServiceApplication {

    /**
     * Método principal que inicia a aplicação Spring Boot.
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        SpringApplication.run(HistoricoServiceApplication.class, args);
    }

}
