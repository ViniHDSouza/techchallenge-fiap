# Guia Passo a Passo para Testes - Sistema Hospitalar (Kafka)

Este guia detalha como testar as funcionalidades do Sistema Hospitalar implementado com microserviços e Kafka.

**Pré-requisitos:**

*   Aplicação rodando via `docker-compose up --build -d` (conforme instruções no `README.md`).
*   Ferramentas como `curl` ou Postman para testar a API REST.
*   Um cliente GraphQL (como o GraphiQL UI embutido, Postman, ou Insomnia) para testar a API GraphQL.
*   (Opcional) Ferramentas para interagir com Kafka (como `kafkacat` ou UI como Offset Explorer/Conduktor) para inspecionar o tópico.

## 1. Testando o Agendamento Service (API REST)

O serviço de agendamento expõe uma API REST na porta `8081` (`http://localhost:8081`). Você pode usar a collection do Postman (`tech_challenge_fase3.postman_collection.json`) fornecida ou seguir os exemplos com `curl`.

**Observação:** Como não há endpoints para criar Pacientes e Médicos diretamente via API neste exemplo (eles são criados no banco de dados para os testes de integração ou podem ser adicionados manualmente), vamos assumir que existem IDs válidos. Você pode verificar os IDs nos bancos de dados `mysql-agendamento` e `mysql-historico` se necessário.

**Exemplo: Criar Paciente e Médico (se não existirem - requer acesso ao DB ou endpoints adicionais)**

*   *Normalmente, você precisaria de endpoints POST /pacientes e POST /medicos, ou inserir dados diretamente no banco `mysql-agendamento`.*
*   Para este guia, vamos assumir que o Paciente com ID `1` e o Médico com ID `1` existem (podem ter sido criados pelos testes de integração ou manualmente).

**a) Agendar uma Nova Consulta (POST /consultas)**

*   **Objetivo:** Criar uma nova consulta e verificar se uma mensagem é enviada ao Kafka.
*   **Comando `curl`:**
    ```bash
    curl -X POST http://localhost:8081/consultas \
    -H "Content-Type: application/json" \
    -d 
{
    "paciente": {"id": 1}, 
    "medico": {"id": 1}, 
    "dataHora": "2025-12-10T10:00:00", 
    "observacoes": "Consulta de rotina via curl"
}
    
    ```
*   **Verificação:**
    *   A resposta deve ser `201 Created` com os detalhes da consulta criada (incluindo o ID gerado).
    *   **Kafka:** Verifique o tópico `consultas.topic`. Uma nova mensagem deve aparecer contendo os detalhes da consulta e `"tipoNotificacao":"CRIACAO"`.
    *   **Notificação Service:** Verifique os logs do container `notificacao-service` (`docker-compose logs -f notificacao-service`). Você deve ver logs indicando o recebimento da mensagem e a simulação do envio da notificação.
    *   **Histórico Service (GraphQL):** Após um tempo (dependendo da estratégia de sincronização, que não está implementada aqui), a consulta deveria aparecer no histórico. Neste exemplo, como os bancos são separados, a consulta *não* aparecerá automaticamente no histórico via GraphQL sem sincronização.

**b) Buscar Consulta por ID (GET /consultas/{id})**

*   **Objetivo:** Recuperar os detalhes de uma consulta específica.
*   **Comando `curl` (substitua `{id_da_consulta}` pelo ID retornado no passo anterior):**
    ```bash
    curl http://localhost:8081/consultas/{id_da_consulta}
    ```
*   **Verificação:** A resposta deve ser `200 OK` com os detalhes da consulta.

**c) Listar Todas as Consultas (GET /consultas)**

*   **Objetivo:** Listar todas as consultas agendadas.
*   **Comando `curl`:**
    ```bash
    curl http://localhost:8081/consultas
    ```
*   **Verificação:** A resposta deve ser `200 OK` com uma lista de todas as consultas no banco `agendamento_db`.

**d) Atualizar uma Consulta (PUT /consultas/{id})**

*   **Objetivo:** Modificar uma consulta existente (ex: remarcar) e verificar se uma mensagem de atualização é enviada ao Kafka.
*   **Comando `curl` (substitua `{id_da_consulta}`):**
    ```bash
    curl -X PUT http://localhost:8081/consultas/{id_da_consulta} \
    -H "Content-Type: application/json" \
    -d 
{
    "paciente": {"id": 1}, 
    "medico": {"id": 1}, 
    "dataHora": "2025-12-11T11:30:00", 
    "observacoes": "Consulta REMARCADA via curl"
}
    
    ```
*   **Verificação:**
    *   A resposta deve ser `200 OK` com os detalhes da consulta atualizada.
    *   **Kafka:** Verifique o tópico `consultas.topic`. Uma nova mensagem deve aparecer contendo os detalhes atualizados e `"tipoNotificacao":"ATUALIZACAO"`.
    *   **Notificação Service:** Verifique os logs do `notificacao-service` para a simulação da notificação de remarcação.

**e) Excluir uma Consulta (DELETE /consultas/{id})**

*   **Objetivo:** Remover uma consulta.
*   **Comando `curl` (substitua `{id_da_consulta}`):**
    ```bash
    curl -X DELETE http://localhost:8081/consultas/{id_da_consulta}
    ```
*   **Verificação:**
    *   A resposta deve ser `204 No Content`.
    *   Tentar buscar a consulta novamente (GET /consultas/{id_da_consulta}) deve retornar `404 Not Found`.
    *   **Observação:** Nenhuma mensagem Kafka é enviada na exclusão neste fluxo.

## 2. Testando o Notificação Service (Consumo Kafka)

Este serviço não possui API externa. O teste consiste em verificar seus logs após a criação ou atualização de consultas no Agendamento Service.

*   **Comando:**
    ```bash
    docker-compose logs -f notificacao-service
    ```
*   **Verificação:** Após criar ou atualizar uma consulta (passos 1.a e 1.d), observe os logs. Você deve ver mensagens como:
    *   `Mensagem recebida do Kafka no tópico...`
    *   `--- SIMULANDO ENVIO DE NOTIFICAÇÃO ---`
    *   `Para: [Nome Paciente] ([Email Paciente])`
    *   `Mensagem: Sua consulta ... foi agendada/remarcada...`
    *   `--- FIM DA SIMULAÇÃO ---`
    *   `Notificação processada com sucesso para consulta ID: ...`

## 3. Testando o Histórico Service (API GraphQL)

O serviço de histórico expõe uma API GraphQL na porta `8083` (`http://localhost:8083/graphql`). A maneira mais fácil de testar é usando a interface GraphiQL embutida: `http://localhost:8083/graphiql`.

**Observação:** Lembre-se que os dados aqui refletem o banco `historico_db`. Sem um mecanismo de sincronização, ele não será atualizado automaticamente com as operações do `agendamento-service`.

**Exemplo: Popular dados no `historico_db` (se necessário)**

*   Você pode usar um cliente MySQL conectado à porta `3307` (usuário `user`, senha `password`, database `historico_db`) para inserir dados de Pacientes, Médicos e Consultas para teste.

**a) Buscar Histórico de Consultas por Paciente**

*   **Objetivo:** Listar todas as consultas de um paciente específico no banco de histórico.
*   **Query GraphQL (substitua `1` pelo ID do paciente desejado):**
    ```graphql
    query {
      consultasPorPaciente(pacienteId: "1") {
        id
        dataHora
        status
        observacoes
        paciente {
          nome
          email
        }
        medico {
          nome
          especialidade
        }
        enfermeiro {
          nome
          coren
        }
      }
    }
    ```
*   **Verificação:** A resposta deve conter a lista de consultas associadas ao paciente no banco `historico_db`.

**b) Buscar Consultas Futuras por Paciente**

*   **Objetivo:** Listar apenas as consultas futuras de um paciente.
*   **Query GraphQL (substitua `1` pelo ID do paciente):**
    ```graphql
    query {
      consultasFuturasPorPaciente(pacienteId: "1") {
        id
        dataHora
        status
        paciente {
          nome
        }
      }
    }
    ```
*   **Verificação:** A resposta deve conter apenas as consultas cuja `dataHora` seja posterior ao momento da consulta.

**c) Buscar Consulta Específica por ID**

*   **Objetivo:** Obter detalhes de uma única consulta pelo seu ID no histórico.
*   **Query GraphQL (substitua `1` pelo ID da consulta):**
    ```graphql
    query {
      consulta(id: "1") {
        id
        dataHora
        status
        observacoes
        paciente { nome }
        medico { nome }
        enfermeiro { nome }
      }
    }
    ```
*   **Verificação:** A resposta deve conter os detalhes da consulta ou ser `null` se não encontrada.

**d) Listar Todos os Pacientes no Histórico**

*   **Objetivo:** Obter a lista de todos os pacientes registrados no banco `historico_db`.
*   **Query GraphQL:**
    ```graphql
    query {
      pacientes {
        id
        nome
        email
        telefone
      }
    }
    ```
*   **Verificação:** A resposta deve conter a lista de pacientes.

**e) Listar Todos os Médicos no Histórico**

*   **Objetivo:** Obter a lista de todos os médicos registrados no banco `historico_db`.
*   **Query GraphQL:**
    ```graphql
    query {
      medicos {
        id
        nome
        especialidade
        crm
      }
    }
    ```
*   **Verificação:** A resposta deve conter a lista de médicos.

## 4. Verificando Cobertura de Testes (JaCoCo)

Após executar os testes (unitários e de integração) localmente com Maven, você pode gerar os relatórios de cobertura.

1.  **Execute os testes para um serviço específico:**
    Navegue até o diretório do serviço (ex: `cd agendamento-service`) e execute:
    ```bash
    mvn clean verify
    ```
    *   O `verify` garante que a fase `test` seja executada e o plugin JaCoCo gere o relatório.

2.  **Acesse o relatório:**
    O relatório HTML do JaCoCo estará localizado em `target/site/jacoco/index.html` dentro do diretório de cada serviço.

3.  **Repita para os outros serviços** (`notificacao-service`, `historico-service`).

Este guia cobre os principais fluxos de teste da aplicação. Adapte os IDs e dados conforme necessário para seus testes específicos.
