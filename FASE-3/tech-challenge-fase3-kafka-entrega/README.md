# Tech Challenge Fase 3 - Sistema Hospitalar (Kafka)

Este projeto implementa um sistema hospitalar simplificado como parte do Tech Challenge Fase 3, utilizando uma arquitetura de microserviços com comunicação assíncrona via Apache Kafka.

## Arquitetura

O sistema é composto pelos seguintes microserviços:

1.  **Agendamento Service (`agendamento-service`):**
    *   Responsável pelo agendamento, atualização e cancelamento de consultas.
    *   Expõe uma API REST para gerenciar consultas, pacientes, médicos e enfermeiros (operações básicas de CRUD podem ser adicionadas se necessário).
    *   Persiste os dados em um banco de dados MySQL (`agendamento_db`).
    *   **Produz** mensagens no tópico Kafka `consultas.topic` sempre que uma consulta é criada ou atualizada, enviando um `ConsultaNotificacaoDTO`.
    *   Porta: `8081`

2.  **Notificação Service (`notificacao-service`):**
    *   Responsável por simular o envio de notificações aos pacientes sobre suas consultas.
    *   **Consome** mensagens do tópico Kafka `consultas.topic`.
    *   Ao receber uma mensagem, processa o `ConsultaNotificacaoDTO` e simula o envio de uma notificação (atualmente, apenas loga a ação).
    *   Não possui persistência própria neste exemplo.
    *   Porta: `8082`

3.  **Histórico Service (`historico-service`):**
    *   Responsável por fornecer um histórico de consultas e dados relacionados.
    *   Expõe uma API GraphQL para consultar informações de pacientes, médicos, enfermeiros e consultas.
    *   Persiste os dados em um banco de dados MySQL (`historico_db`). Assume-se que os dados são sincronizados ou populados de forma independente para fins de consulta histórica (neste exemplo, as tabelas são as mesmas do agendamento para simplificar).
    *   Porta: `8083`
    *   Endpoint GraphQL: `/graphql`
    *   Interface GraphiQL (para testes): `/graphiql`

## Tecnologias Utilizadas

*   **Linguagem:** Java 17
*   **Framework:** Spring Boot 3.2.5
*   **Persistência:** Spring Data JPA, Hibernate
*   **Banco de Dados:** MySQL 8.0
*   **Comunicação Assíncrona:** Apache Kafka (via Spring Kafka)
*   **API Histórico:** GraphQL (via Spring for GraphQL)
*   **API Agendamento:** REST (via Spring Web)
*   **Build:** Maven
*   **Testes:** JUnit 5, Mockito, Spring Boot Test, Spring Kafka Test, GraphQL Test
*   **Cobertura de Testes:** JaCoCo
*   **Containerização:** Docker, Docker Compose
*   **Outros:** Lombok, SLF4j (Logging)

## Configuração e Execução (Docker Compose)

A forma mais simples de executar a aplicação completa é utilizando Docker Compose.

**Pré-requisitos:**

*   Docker
*   Docker Compose
*   Maven (apenas para build local, se não usar Docker Compose para build)
*   Java 17 (apenas para desenvolvimento local)

**Passos:**

1.  **Clone o repositório:**
    ```bash
    git clone <url-do-repositorio>
    cd tech-challenge-fase3-kafka
    ```

2.  **Construa e suba os containers:**
    Navegue até o diretório raiz do projeto (`tech-challenge-fase3-kafka`) onde o arquivo `docker-compose.yml` está localizado e execute:
    ```bash
    docker-compose up --build -d
    ```
    *   `--build`: Força a reconstrução das imagens dos microserviços a partir dos Dockerfiles.
    *   `-d`: Executa os containers em background (detached mode).

3.  **Aguarde a inicialização:** Pode levar alguns minutos para todos os serviços (Kafka, Zookeeper, MySQL, microserviços) iniciarem completamente. Você pode verificar os logs com:
    ```bash
    docker-compose logs -f [nome-do-servico] # Ex: docker-compose logs -f agendamento-service
    ```

4.  **Acesse os serviços:**
    *   **Agendamento Service (REST):** `http://localhost:8081`
    *   **Notificação Service:** (Não possui endpoint HTTP direto, consome Kafka)
    *   **Histórico Service (GraphQL):**
        *   Endpoint: `http://localhost:8083/graphql`
        *   GraphiQL UI: `http://localhost:8083/graphiql`
    *   **Kafka:** `localhost:9092` (interno), `localhost:29092` (externo)
    *   **MySQL Agendamento:** `localhost:3306`
    *   **MySQL Histórico:** `localhost:3307`

5.  **Parar os containers:**
    ```bash
    docker-compose down
    ```
    *   Para remover os volumes (dados dos bancos) também, use: `docker-compose down -v`

## Estrutura do Projeto

```
tech-challenge-fase3-kafka/
├── agendamento-service/      # Microserviço de Agendamento
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── historico-service/        # Microserviço de Histórico
│   ├── src/
│   │   └── main/
│   │       └── resources/
│   │           └── graphql/
│   │               └── schema.graphqls  # Schema GraphQL
│   ├── pom.xml
│   └── Dockerfile
├── notificacao-service/      # Microserviço de Notificação
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yml        # Orquestração dos containers
├── GUIA_TESTES.md            # Guia passo a passo para testes
├── README.md                 # Este arquivo
└── tech_challenge_fase3.postman_collection.json # Collection Postman (Agendamento API)
```

## Observações

*   **Comentários e Logs:** Todo o código-fonte inclui comentários e gera logs em **português do Brasil**.
*   **Autenticação:** Conforme solicitado, não foi implementada autenticação/autorização (Spring Security).
*   **Sincronização de Dados:** Este exemplo assume que os dados entre `agendamento-service` e `historico-service` são consistentes. Em um cenário real, seria necessária uma estratégia de sincronização (ex: CDC com Debezium, eventos Kafka adicionais, API interna).
*   **Tratamento de Erros Kafka:** A implementação atual possui tratamento básico de erros no producer e consumer Kafka (logs). Em produção, estratégias mais robustas como retentativas e Dead Letter Queues (DLQ) seriam recomendadas.
*   **Testes:** Foram implementados testes unitários e de integração, incluindo testes com Kafka embarcado e GraphQL Tester. A cobertura de testes é monitorada via JaCoCo.

