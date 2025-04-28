package br.com.fiap.techchallenge.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal para inicialização do Microserviço de Notificações.
 */
@SpringBootApplication
public class NotificacaoServiceApplication {

    /**
     * Método principal que inicia a aplicação Spring Boot.
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificacaoServiceApplication.class, args);
    }

}

