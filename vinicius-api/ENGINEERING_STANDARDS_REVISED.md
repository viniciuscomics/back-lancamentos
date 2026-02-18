# ENGINEERING_STANDARDS.md

# Sumário

1. [Visão Geral e Stack](#1-visão-geral-e-stack)
2. [Estrutura Visual de Pastas](#2-estrutura-visual-de-pastas)
3. [Fluxo de Dados e Comunicação](#3-fluxo-de-dados-e-comunicação)
    * 3.1. [Camadas de Domínio (Pureza)](#31-camadas-de-domínio-pureza)
    * 3.2. [UseCases como Maestros](#32-usecases-como-maestros)
    * 3.3. [Infraestrutura e Adapters](#33-infraestrutura-e-adapters)
    * 3.4. [O Papel dos Mappers](#34-o-papel-dos-mappers)
4. [Padronização de Respostas (Result Pattern)](#4-padronização-de-respostas-result-pattern)
5. [Padrões de Testes](#5-padrões-de-testes)
    * 5.1. [Estrutura Visual de Testes](#51-estrutura-visual-de-testes)
    * 5.2. [Testes de Integração (Jornadas)](#52-testes-de-integração-jornadas)
    * 5.3. [Testes Unitários (Domínio)](#53-testes-unitários-domínio)
    * 5.4. [Guia de Responsabilidades e "Porquês"](#54-guia-de-responsabilidades-e-porquês)
6. [Observabilidade e Resiliência](#6-observabilidade-e-resiliência)
    * 6.1. [Estratégia de Captura de Erros](#61-estratégia-de-captura-de-erros)
    * 6.2. [Política de Logs (Otimização de Custo)](#62-política-de-logs-otimização-de-custo)
    * 6.3. [Contratos de Saída (DTOs)](#63-contratos-de-saída-dtos)
7. [Validações e Regras de Negócio](#7-validações-e-regras-de-negócio)
    * 7.1. [UseCase Magro e Entidade Rica](#71-usecase-magro-e-entidade-rica)
    * 7.2. [Camadas de Validação](#72-camadas-de-validação)
    * 7.3. [Substituindo Pacotes Utils por Value Objects](#73-substituindo-pacotes-utils-por-value-objects)

## 1. Visão Geral e Stack
* **Linguagem:** Java 21 (LTS) - Uso obrigatório de `Records`, `Sealed Interfaces` e `Pattern Matching`.
* **Framework:** Spring Boot 3.3+.
* **Imutabilidade:**
    * **Entidades JPA (`infra/persistence`):** Devem ser classes padrão para compatibilidade com Hibernate/JPA.
    * **O Restante:** Uso obrigatório de `Records` para Entidades de Domínio, DTOs e Value Objects.
* **Padrão de Resposta:** Funcional via `Result<T>`.

---

## 2. Estrutura Visual de Pastas
Esta estrutura visa o desacoplamento total entre a lógica de negócio e as ferramentas tecnológicas.

```plaintext
src/main/java/com/empresa/projeto/
│
├── domain/                      # Onde a empresa ganha dinheiro (Lógica Pura)
│   ├── core/                    # Objetos globais (ex: Result.java, ErrorType.java)
│   ├── model/                   # Entidades Ricas (Records - ex: User.java com métodos de negócio)
│   ├── event/                   # Um `record` imutável que representa o fato ocorrido (ex: `UserCreatedEvent`).
│   ├── usecase/                 # Orquestradores de fluxo (@Component + @Transactional)
│   ├── usecase/dto              # Records para entrada/saída de dados(vindo da controller / retornado para controller)
│   ├── service/                 # Regras complexas multi-domínio (Somente se necessário)
│   ├── repository/              # Interfaces de persistência (Ports)
│   └── exception/               # Exceções de negócio (Casos específicos)
│
├── application/                 # Porta de entrada dos usuários
│   ├── controller/              # Endpoints REST (Swagger Annotations + Orquestração)
│   ├── dto/                     # Records para entrada de dados- (somente em casos especificos, e se for feito o mesmo deve ser mapeado para camada de dominio.)
│   ├── listener/                # A classe que escuta o evento e chama o próximo UseCase.
│   └── mapper/                  # Tradutores (ErrorMapper)
│
└── infra/                       # Detalhes técnicos e ferramentas (Adapters)
    ├── config/                  # Beans de configuração, Security, Swagger
    ├── persistence/             # Implementações JPA/Hibernate (Classes padrão)
    ├── external/                # Clientes para APIs de terceiros (Feign/RestClient)
    ├── messaging/               # Listeners de filas e produtores (Kafka/RabbitMQ)
    ├── scheduler/               # Agendamentos de tarefas (@Scheduled)
    └── exception/               # GlobalExceptionHandler (Rede de proteção técnica)
```

---

## 3. Guia de Responsabilidades (O "Porquê")

### 3.1. Domínio Rico vs. Services
* **Entidade (Cérebro):** Deve ser "rica". Se uma validação ou cálculo depende apenas dos dados internos da classe (ex: `pedido.podeSerCancelado()`), essa lógica **pertence obrigatoriamente** à Entidade. No nosso padrão, entidades de domínio são `Records`.
* **Entidade JPA:** Classes padrão localizadas em `infra/persistence`. Elas são apenas para persistência e não devem conter lógica de negócio.
* **Decisão:** A lógica deve morar na Entidade de Domínio (`domain/model`). Services só existem se houver complexidade multi-domínio.
* **Porquê:** Evita o "Modelo Anêmico" (classes de dados vazias). Facilita testes unitários sem precisar subir o Spring.
* **O Domínio:** é o centro. Nenhuma classe dentro de domain/ pode importar classes de application/ ou infra/. Se o domínio precisar de um serviço externo, ele define uma interface; a infraestrutura a implementa.
* **Exemplo:** Um método `user.isAdult()` na entidade é melhor que `userService.checkIfUserIsAdult(user)`.


### 3.2. UseCases como Maestros
* **UseCase (Orquestrador):** Deve ser "magro" e deve ter apenas um metodo publico "execute". Sua única responsabilidade é buscar os dados necessários (Infra), chamar os métodos de negócio da Entidade (Domain) e persistir o resultado.
* **Anotações:** Devem ser anotados com `@Component` para gerenciamento do Spring e `@Transactional` no metodo execute para garantir a atomicidade das operações de banco de dados.
* **Decisão:** Cada ação do sistema é um UseCase (`domain/usecase`).
* **Porquê:** Segue o Princípio de Responsabilidade Única (SRP). Facilita a leitura do que o sistema faz (ex: `ProcessPromotionUseCase`).
* **Fluxo:** 'Controller (DTO) -> UseCase (Domain Record) -> Repository Port -> Infra Repository -> JPA Entity (Class).'
* **UseCases** de consulta devem usar @Transactional(readOnly = true). UseCases de escrita devem ser @Transactional.
* **Proibido** retornar null: Um UseCase nunca retorna null. Ele retorna Success(Optional.empty()) ou Failure(NOT_FOUND).
* **Para evitar redundância**, os Records de Request e Response que definem a entrada e saída do sistema devem residir no Domínio (junto ao UseCase). A camada de Aplicação deve reutilizar esses Records para o recebimento de dados via API, garantindo que o contrato de entrada seja definido pela necessidade do negócio e não pela ferramenta web.
* **Cada UseCase** deve preferencialmente ter seu próprio par de Request/Response records. Evite o reaproveitamento de DTOs entre UseCases diferentes para evitar acoplamento de contratos (Side Effects).
* **Importante:** Priorize o estilo funcional (map, flatMap, recover) dentro dos UseCases, deixando o "desempacotamento" do Result (o switch ou if) exclusivamente para o ErrorMapper na Controller.
* **Exemplo:** O UseCase busca no Provider, aplica a regra na Entidade e manda o Provider salvar.

```java
	@Component
	@DisplayName("UseCase: Cancelar Pedido")
	public class CancelOrderUseCase {
	
	    private final UserRepository repository;
	
	    @Transactional
		public Result<Order> execute(Long orderId) {
			return repository.findById(orderId)                // 1. Orquestra a busca				
				.flatMap(order -> order.cancel())              // 2. A lógica REAL está na Entidade
				.flatMap(repository::save);                    // 3. Orquestra a persistência
		}
	}
```

### B. Orquestração de Eventos (Side Effects)

Sempre que uma jornada de negócio exigir que ações secundárias aconteçam (ex: enviar e-mail após cadastro, notificar outro sistema, limpar cache), devemos utilizar **Eventos de Domínio**.

### B.1. A Regra de Ouro
Um UseCase **nunca** deve injetar outro UseCase. Se o UseCase A precisa que o UseCase B execute, o A deve disparar um evento e um `Listener` deve capturar esse evento para chamar o B.

### B.2 Onde moram os componentes?
1.  **Evento (`domain/event`):** Um `record` imutável que representa o fato ocorrido (ex: `UserCreatedEvent`).
2.  **Porta de Disparo (`domain/ports` ou `application/ports`):** Uma interface que o UseCase usará (ex: `EventPublisher`).
3.  **Implementação (`infra/messaging`):** A implementação real (usando `ApplicationEventPublisher` do Spring ou RabbitMQ/Kafka).
4.  **Listener (`application/listener`):** A classe que escuta o evento e chama o próximo UseCase.

### B.3 Exemplo de Fluxo no UseCase

```java
public class CreateUserUseCase {
    private final UserRepository repository;
    private final EventPublisher publisher; // Interface, não a implementação!

    public Result<User> execute(User user) {
        return repository.save(user)
            .peek(savedUser -> publisher.publish(new UserCreatedEvent(savedUser.id())));
    }
}
```


### 3.3. A Camada de Infra (Adapters)
Aqui moram as implementações que dependem de bibliotecas externas.

* **Schedulers (`infra/scheduler`):** São apenas gatilhos. Não devem ter `if` ou `for`. Eles "acordam" e chamam um UseCase.
* **Messaging/Listeners (`infra/messaging`):** Traduzem uma mensagem (JSON do Kafka) em um objeto de domínio e chamam o UseCase.
* **Porquê:** Se trocarmos o Kafka pelo RabbitMQ, ou o Cron pelo AWS Lambda, o seu Domínio (regras de negócio) permanece intacto.
* **Importante:** a Interface (Port) fica em domain/repository e deve se chamar Repository (ex: UserRepository). A Implementação (Adapter) fica em infra/persistence e pode se chamar Provider ou Adapter.

Para garantir que a lógica não vaze, um Scheduler deve ser assim:

```java
// Local: infra/scheduler/CleanOldLogsJob.java

@Scheduled(cron = "0 0 0 * * *")
public void run() {
    // A infra apenas aciona o comando
    cleanLogsUseCase.execute(); 
}
```

#### provider
```java
@Component
@RequiredArgsConstructor
public class UserProvider implements UserRepository {
    private final JpaOrderRepository jpaRepository; // O repositório do Spring Data
    private final UserMapper mapper;

    @Override
    public Result<User> findById(Long id) {
        // 1. Busca o Optional do JPA
        // 2. Converte para Result usando o ErrorType (conforme solicitado)
        // 3. Mapeia a Entidade de Infra para o Objeto de Domínio
        return Result.toResult(jpaRepository.findById(id), ErrorType.NOT_FOUND)
                     .map(mapper::toDomain);
    }

    @Override
    public Result<User> save(User user) {
        var entity = mapper.toEntity(user);
        var savedEntity = jpaRepository.save(entity);
        return new Result.Success<>(mapper.toDomain(savedEntity));
    }
}

```

### 3.4. Mappers de Fronteira
* **Decisão:** Mapear dados em cada entrada e saída de camada.
* **Porquê:** Impede que anotações técnicas (como `@JsonProperty` ou `@Id` do JPA) poluam o domínio. O Domínio deve ser Java puro.
* **Padronização:** Prefira o uso de MapStruct. Caso a lógica seja complexa demais para o MapStruct, a implementação manual deve residir na mesma estrutura de pastas, mantendo a nomenclatura Mapper.
                    Os mappers devem sempre produzir novos objetos (Records ou Entidades novas) em vez de realizar mutação em objetos existentes, a menos que seja um mapeamento de atualização (@MappingTarget).

---
Para garantir o desacoplamento total entre o mundo externo (API/Web) e o mundo interno (Banco de Dados/Persistência), é obrigatória a separação dos Mappers em duas categorias distintas. O Agente de IA nunca deve utilizar o mesmo Mapper para converter um DTO de entrada diretamente em uma Entidade JPA.

### A. Mapeamento de Aplicação (`application/mapper`)
Responsável por converter o **Domain Record** para o **Response DTO** (e vice-versa para Requests).
* **Objetivo:** Isolar o domínio de detalhes da Web (ex: formatos de data JSON, nomes de campos específicos da API).
* **Uso:** Exclusivo dentro das Controllers ou no final do UseCase.

```java
@Mapper(componentModel = "spring")
public interface UserApplicationMapper {
    // Converte o Domínio (Solo Sagrado) para o contrato final da API (DTO)
    UserResponse toResponse(User domain);    
}
```

### B. Mapeamento de Persistência (infra/persistence/mapper)
Responsável por converter o Domain Record para a JPA Entity (e vice-versa).
* **Objetivo:** Isolar o domínio das anotações de infraestrutura (ex: @Entity, @Table, @Id).
* **Uso:** Exclusivo dentro dos Providers/Adapters de persistência.

```
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    // Converte o que vem do banco (Entity) para o Record Puro do Domínio
    User toDomain(UserEntity entity);
    
    // Converte o Record do Domínio para a Entidade gerida pelo Hibernate
    UserEntity toEntity(User domain);
}
```

## 4. Tratamento de Erros: Result Pattern
Em vez de `throw exception`, usamos o retorno `Result<T>` para fluxos de negócio.

### 4.1. Implementação do Result
```java
public sealed interface Result<T> {
    record Success<T>(T value) implements Result<T> {}
	
    record Failure<T>(
        String message, 
        ErrorType type, 
        List<ErrorDetail> errors, // Lista para múltiplas falhas (ex: validação)
        Throwable cause
    ) implements Result<T> {
        // Construtor auxiliar para erros simples sem detalhes de campos
        public Failure(String message, ErrorType type) {
            this(message, type, List.of(), null);
        }
    }
	
	/**
     * Representa um erro específico em um campo (útil para validações de entrada).
     */
    record ErrorDetail(String field, String message) {}

    // --- FÁBRICA DE CONVERSÃO ---

    /**
     * Converte um Optional para Result usando apenas o ErrorType.
     * A mensagem é extraída automaticamente do Enum.
     */
    static <T> Result<T> toResult(Optional<T> optional, ErrorType type) {
        return optional
            .<Result<T>>map(Success::new)
            .orElseGet(() -> new Failure<>(type.getMessage(), type, null));
    }
	
    // Métodos que o Agente DEVE usar para transformar dados
    default <R> Result<R> map(Function<T, R> mapper) {
        if (this instanceof Success<T> s) return new Success<>(mapper.apply(s.value()));
        return (Result<R>) this;
    }

    default <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (this instanceof Success<T> s) return mapper.apply(s.value());
        return (Result<R>) this;
    }
	
	enum ErrorType {
        NOT_FOUND, VALIDATION, CONFLICT, UNAUTHORIZED, FORBIDDEN, UNPROCESSABLE_ENTITY
    }
}
```

### 4.2. ErrorMapper (Controller)
Para converter falhas em respostas HTTP na camada de `application/controller`:
```java
public class ErrorMapper {
    public static ResponseEntity<Object> toResponseEntity(Result.Failure<?> failure) {
        var status = getHttpStatus(failure.type());

        var errorDto = ErrorDTO.builder()
            .type("about:blank")
            .title(failure.type().name())
            .status(status.value())
            .detail(failure.message())
            .errors(failure.errors()) // Repassa a lista de erros detalhados
            .build();

        return ResponseEntity.status(status).body(errorDto);
    }

    private static HttpStatus getHttpStatus(ErrorType type) {
        return switch (type) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE_ENTITY -> HttpStatus.UNPROCESSABLE_ENTITY;
			case FORBIDDEN -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
```
* **Regra:** Se o erro é uma situação esperada (Usuário não encontrado, Saldo insuficiente), retorne `Result.Failure`. Erros técnicos inesperados (Runtime) devem subir para o `GlobalExceptionHandler`.
* **Use o padrao Problem Details for HTTP APIs (RFC 7807). Em vez de apenas uma mensagem de erro, o DTO de erro ErrorDTO pode ter type, title, status e detail. Isso facilita muito a vida de quem consome a API (Front-end/Mobile).

### 4.3. Matriz de Decisão: Result vs. Exception

O Agente de IA deve seguir esta matriz para decidir se retorna um `Result.Failure` ou se lança uma `Exception`. A regra de ouro é: **Result** para fluxos de negócio esperados; **Exception** para falhas técnicas catastróficas.

| Cenário | Mecanismo | Categoria | Comportamento Esperado |
| :--- | :--- | :--- | :--- |
| **Regra de Negócio** (ex: Saldo insuficiente) | `Result.Failure` | Negócio | Retornar erro tratado com `ErrorType.VALIDATION`. |
| **Recurso Inexistente** (404 no DB ou API externa) | `Result.Failure` | Negócio | Converter o 404 em `Result.Failure(NOT_FOUND)`. |
| **Conflito/Duplicidade** (ex: E-mail já cadastrado) | `Result.Failure` | Negócio | Retornar `Result.Failure(CONFLICT)`. |
| **Erro de Cliente Externo** (4xx em API externa) | `Result.Failure` | Negócio | Tratar o erro da API externa e mapear para uma falha de negócio. |
| **Falha de Integração** (5xx em API externa) | `Exception` | Infra | Deixar a exceção subir (ou lançar `IntegrationException`) para o Global Handler. |
| **Timeout / Rede** (API externa fora do ar) | `Exception` | Infra | Lançar exceção técnica. O sistema não pode cumprir o contrato. |
| **Erro Crítico de Infra** (Banco fora, Disk Full) | `Exception` | Infra | Interromper o fluxo imediatamente via RuntimeException. |

#### Regras de Implementação para o Agente:
Ao implementar um Client para uma API externa:

1.  **Erros 4xx (Client Errors):** Devem ser capturados no Adapter de Infra e convertidos para um `Result.Failure` de domínio. Isso indica que a integração "funcionou", mas a regra de negócio do parceiro recusou a operação.
2.  **Erros 5xx (Server Errors) ou Timeouts:** Não devem ser "abafados". Deixe que a infraestrutura trate como um erro técnico (Retry, Circuit Breaker ou resposta 500 para o usuário final).
3.  **Mensagens de Erro:** As mensagens contidas no `Result.Failure` devem ser amigáveis para o usuário final ou seguir o padrão definido no enum `ErrorType`.
---

## 5. Padrões de Testes

#### 5.1. Estrutura Visual de Testes
A estrutura de pastas deve isolar a lógica de negócio (Unitários) da infraestrutura (Jornadas), garantindo que os testes de integração tenham acesso às massas de dados estáticas.

```plaintext
src/test/java/com/empresa/projeto/
│
├── domain/                      # Testes Unitários (Lógica Pura de Negócio)
│   ├── model/                   # Valida regras internas das Entidades Ricas
│   └── usecase/                 # Valida fluxo de negócio (Mocks de Repository)
│
├── application/                 # Testes de Jornada (Integração)
│   └── controller/              # Endpoints + UseCase + Infra
│
└── BaseIntegrationTest.java     # Configuração base de infraestrutura (Containers)

src/test/resources/
├── __files/                     # Payloads JSON (Corpos das respostas das APIs)
└── mappings/                    # Definições de Match (URL, Verbo, Params para WireMock)
```

### 5.2. Testes de Integração (Jornadas)
**Objetivo:** Validar o fluxo completo ("end-to-app-end"). O teste deve garantir que a `application` orquestre corretamente o `domain` e que a `infra` persiste ou consome dados reais.
Utilizamos o **WireMock** de forma declarativa para simular dependências externas.
* **Dependência:** `spring-cloud-starter-contract-stub-runner`.
* **Regra:** O WireMock intercepta chamadas automaticamente com base nos arquivos em `src/test/resources/mappings/`.

#### O Mapeamento (src/test/resources/mappings/check-credit.json):

```
{
  "request": {
    "method": "GET",
    "urlPath": "/v1/credit-check/123"
  },
  "response": {
    "status": 200,
    "bodyFileName": "external/credit-success.json",
    "headers": { "Content-Type": "application/json" }
  }
}
```
---

#### Classe Base de Integração
Centraliza a subida do Docker (Testcontainers) para evitar o custo de inicialização entre cada classe de teste.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // Gerencia banco de dados real (Postgres)- ou qualquer outro banco que venha a utilizar na sua aplicação
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }
}
```

#### Exemplo 1: Jornada Simples (Banco de Dados)
Foco em validar se a regra de negócio foi refletida no banco real através do Repository.

```java
@DisplayName("Jornada: Cadastro de Usuário")
class CreateUserJourneyTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("Deve persistir um novo usuário no banco real ao receber payload válido")
    void shouldCreateUserSuccessfully() throws Exception {
        String json = "{\"name\": \"Gabriel\", \"email\": \"gabriel@empresa.com\"}";

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        // Validação via Result Pattern: o dado está no banco real?
        var savedUser = userRepository.findByEmail("gabriel@empresa.com");
        assertTrue(savedUser.isSuccess());
    }
}
```

#### Exemplo 2: Jornada com API Externa (WireMock Declarativo)
O WireMock intercepta chamadas automaticamente com base nos arquivos em `src/test/resources/mappings/`. **Não use `stubFor` no código Java.**

```java
// O WireMock carrega automaticamente os mappings e __files do classpath de teste
@AutoConfigureWireMock(port = 8089) 
@DisplayName("Jornada: Processamento de Pagamento")
class RegisterPaymentJourneyTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("Deve aprovar pagamento quando o provedor externo retornar sucesso via mapping JSON")
    void shouldProcessPaymentWithExternalValidation() throws Exception {
        // O match ocorre via arquivo JSON em mappings/ correspondente à URL chamada
        mockMvc.perform(post("/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100, \"orderId\": \"ABC\"}"))
                .andExpect(status().isOk());
    }
}
```

---

### 5.3. Testes Unitários (Domínio)
**Objetivo:** Validar a lógica pura de negócio com velocidade máxima. Estes testes **não podem** carregar o contexto do Spring (sem `@SpringBootTest`).

* **Entidades (`domain/model`):** Testar métodos de negócio (ex: `user.isAdult()`) usando Java puro e Instancio para massa de dados.
* **Garantir que campos críticos (como e-mails ou CPFs) sejam sobrescritos com valores válidos usando .set(field(...), valor), para evitar que falhas de validação sintática invalidem o teste unitário.
* **UseCases (`domain/usecase`):** Usar Mockito para simular as interfaces (Ports) de `repository` ou `external`.

#### Exemplo de Teste de UseCase com Result Pattern e Instancio

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("UseCase: Criar Usuário")
class CreateUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CreateUserUseCase useCase;

    @Test
    @DisplayName("Deve retornar falha quando o usuário for menor de idade")
    void shouldReturnFailureWhenUserIsUnderage() {
        // O Instancio gera o Record/DTO com dados aleatórios automaticamente
        var userRequest = Instancio.of(UserRequest.class)
                .set(field(UserRequest::birthDate), LocalDate.now().minusYears(17))
                .create();

        // Execução do UseCase
        Result<User> result = useCase.execute(userRequest);

        // Validação explícita do Result Pattern
        assertTrue(result.isFailure());
        assertEquals(ErrorType.VALIDATION_ERROR, result.getError().type());
        verifyNoInteractions(userRepository);
    }
}
```

---


## 6. Observabilidade e Resiliência


### 6.1. Estratégia de Captura de Erros
Dividimos os erros em duas categorias claras para garantir que o sistema seja resiliente e fácil de depurar:

* **Falhas de Negócio (Camada de Infra):** Erros previstos em contratos externos (ex: HTTP 422 - Dados Inválidos ou 404 - Recurso não encontrado). Devem ser capturados no Adapter/Provider, logados como `WARN` (se necessário) e convertidos em `Result.Failure`.
* **Erros Técnicos/Inesperados:** Erros de rede (Timeout), banco de dados fora do ar ou `NullPointerException`. **Não devem ser capturados manualmente.** Deixe a exceção subir (bubbling up) para ser tratada pelo `GlobalExceptionHandler`.

### 6.2. Política de Logs (Otimização de Custo e Ruído)
Para otimizar custos de ingestão de logs em nuvem, o nível `INFO` não deve ser utilizado no contexto da aplicação. Utilizamos a seguinte hierarquia:

* **`DEBUG`:** Para rastreabilidade trivial, depuração de fluxo e detalhes de payloads (ex: "Iniciando processamento do registro ID: 123").
* **`WARN`:** Para situações importantes que **não interrompem** a jornada do usuário (ex: "Serviço de busca de CEP indisponível, usando cache local").
* **`ERROR`:** Para falhas críticas que **interrompem** a jornada. O `GlobalExceptionHandler` deve registrar o log de erro acompanhado do StackTrace completo.

### 6.3. Contratos de Saída (DTOs)
* **Imutabilidade:** Uso obrigatório de `Records` para garantir que os dados não sejam alterados após a criação.
* **Flat Records (Simplicidade):** DTOs de saída devem ser o mais simples possível. Devem conter apenas os campos necessários para o cliente (Frontend/Mobile), evitando o aninhamento desnecessário de objetos.
* **Desacoplamento:** É proibido retornar entidades do domínio ou do banco de dados diretamente na Controller. Use sempre um Mapper para converter o Domínio em um DTO de saída.

---

### 6.4. Guia de Responsabilidades (Observabilidade)

| Regra | O "Porquê" |
| :--- | :--- |
| **DEBUG vs INFO** | Redução de custos operacionais e foco em logs que realmente auxiliam na depuração. |
| **Bubbling Up Exceptions** | Permite que erros críticos sejam centralizados no GlobalHandler, gerando métricas de saúde (Health Checks) automáticas. |
| **Flat DTOs** | payloads menores, maior performance de rede e segurança (impede vazamento de dados internos). |

---

## 7. Validações e Regras de Entrada

### 7.1. Divisão de Responsabilidades
O pacote Utils costuma virar um "depósito de lixo" se não for bem gerenciado. Em uma arquitetura limpa e orientada ao domínio, o segredo é separar validação sintática (formato) de validação semântica (negócio).
Para evitar poluição e garantir a integridade dos dados, seguimos três níveis de validação:

1.  **Validação Sintática (DTO):** Erros de formato simples, campos nulos ou vazios. 
    * **Local:** `domain/usecase/dto` usando anotações `@NotNull`, `@Email`, `@Min`, etc.
2.  **Validação de Formato Complexo (Value Objects):** CPF, CNPJ, Moeda. 
    * **Local:** `domain/model` ou `domain/validation`. Evitamos pacotes `Utils`. Preferimos criar "Value Objects" que validam o dado na criação.
3.  **Validação de Negócio (UseCase):** Regras que dependem de estado ou outros dados (ex: "E-mail já cadastrado").
    * **Local:** `domain/usecase` orquestrando as chamadas na entidade, assim como explicado em [UseCases como Masteros](#32-usecases-como-maestros).

### 7.2. Substituindo Pacotes Utils
É proibida a criação de pacotes `utils` genéricos. As lógicas de auxílio devem ser movidas para:
* **Domain Services/Validation:** Se a lógica for puramente matemática ou de formato (ex: `CpfValidator`).
* **Value Objects:** Se a lógica define o que o dado **é** (ex: um objeto `Cpf` que não aceita valores inválidos).

### 7.3. Exemplo de Fluxo de Validação
```java
package com.empresa.projeto.domain.usecase;

import com.empresa.projeto.domain.core.Result;
import com.empresa.projeto.domain.core.Result.ErrorType;
import com.empresa.projeto.domain.model.Cpf;
import com.empresa.projeto.domain.model.User;
import com.empresa.projeto.domain.repository.UserRepository;
import com.empresa.projeto.domain.usecase.dto.CreateUserRequest;
import com.empresa.projeto.domain.usecase.dto.CreateUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository repository;

    @Transactional
    public Result<CreateUserResponse> execute(CreateUserRequest request) {
        // 1. Valida o CPF transformando-o em Value Object
        return Cpf.create(request.document())
            .flatMap(cpf -> checkIfUserExists(request.email(), cpf))
            .map(cpf -> buildUser(request, cpf))
            .flatMap(repository::save) // Persiste via Port (Interface)
            .map(this::toResponse);    // Converte para o DTO de saída
    }

    private Result<Cpf> checkIfUserExists(String email, Cpf cpf) {
        if (repository.existsByEmailOrCpf(email, cpf.value())) {
            return Result.failure("Usuário já cadastrado", ErrorType.CONFLICT);
        }
        return Result.success(cpf);
    }

    private User buildUser(CreateUserRequest request, Cpf cpf) {
        return User.builder()
            .name(request.name())
            .email(request.email())
            .cpf(cpf)
            .build();
    }

    private CreateUserResponse toResponse(User user) {
        return CreateUserResponse.builder()
            .id(user.email()) // Exemplo
            .name(user.name())
            .build();
    }
}
```

## 7.4. Objetos de Valor (Value Objects) e Smart Constructors

Para garantir a integridade dos dados desde o momento da sua criação e eliminar definitivamente a necessidade de classes "utilitárias" (como `CpfUtils`, `ValidationHelper`, etc.), adotamos o padrão de **Smart Constructors** em **Value Objects**.

### A. O Conceito
Um Value Object (VO) não deve apenas carregar um dado, mas garantir que esse dado seja válido. Em vez de lançar exceções ou permitir a criação de objetos inválidos, utilizamos métodos estáticos de fábrica que retornam um `Result<T>`.

* **Localização:** `domain/vo/`
* **Imutabilidade:** Devem ser sempre `records` do Java.
* **Substituição de Tipos Primitivos:** Sempre que um campo tiver uma regra de validação (ex: E-mail, CPF, Telefone, CEP, Preço Positivo), a IA deve sugerir a criação de um VO em vez de usar String, Integer ou BigDecimal puros no Domínio.
* **Orquestração no UseCase:** A IA deve instanciar o VO logo no início do fluxo do UseCase, utilizando o Result para interromper o processo caso o dado seja inválido.

### B. Exemplo de Implementação

```java
public record Cpf(String value) {
    
    // Construtor privado para forçar o uso do Smart Constructor
    private Cpf {
        // Validações mínimas de integridade podem ficar aqui
    }

    /**
     * Smart Constructor: O único ponto de entrada para criar um CPF.
     */
    public static Result<Cpf> create(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Result.failure("CPF não pode ser vazio", ErrorType.VALIDATION);
        }

        String cleanedCpf = rawValue.replaceAll("\\D", "");

        if (cleanedCpf.length() != 11) {
            return Result.failure("CPF deve conter 11 dígitos", ErrorType.VALIDATION);
        }

        // Se passar em todas as regras de negócio:
        return Result.success(new Cpf(cleanedCpf));
    }
}
```

## 8. Padrão de Construção e Imutabilidade (Builder)

Para garantir consistência, legibilidade e facilitar a manipulação de dados em um ambiente imutável (especialmente com **Java Records**), toda e qualquer construção de DTOs (Request/Response) e Entidades de Domínio deve utilizar o padrão **Builder** do Lombok.

### 8.1. Uso Obrigatório do `toBuilder = true`
Como nossas estruturas são imutáveis, o Agente de IA não deve tentar alterar campos diretamente (o que é impossível em Records). Para casos onde seja necessário "alterar" um objeto, deve-se utilizar a propriedade `toBuilder = true`. Isso permite criar uma nova instância baseada na anterior, modificando apenas os campos necessários.


### 8.2. Diretrizes para o Agente de IA:
* **Anotação:** Sempre decore Records de domínio e DTOs com `@Builder(toBuilder = true)`.
* **Proibição de Construtores:** O Agente não deve utilizar `new X(...)` para objetos com mais de 2 atributos.
* **Evolução de Estado:** Para alterar o estado de uma entidade (ex: ativar um usuário), use o fluxo: `objetoExistente.toBuilder().campoAlterado(novoValor).build()`.

### 8.3. Exemplo Prático de Implementação:

```java
// Definição no Domínio
@Builder(toBuilder = true)
public record Product(
    UUID id,
    String name,
    BigDecimal price,
    boolean active
) {}

// Uso no UseCase (Evoluindo o estado de forma imutável)
public Result<ProductResponse> updatePrice(UUID id, BigDecimal newPrice) {
    return repository.findById(id)
        .map(product -> product.toBuilder()
            .price(newPrice) // Cria nova instância com preço novo
            .build())
        .flatMap(repository::save)
        .map(mapper::toResponse);
}
```