# Plano Técnico: JMC Diagnostic MCP Server

## 1. Stack Tecnológica
* **Linguagem:** Java 21 (LTS)
* **Build Tool:** Maven
* **Framework MCP:** Spring AI MCP Server (`spring-ai-mcp-server-spring-boot-starter`) ou MCP Java SDK oficial (`io.modelcontextprotocol.sdk:mcp`)
* **Core JMC:**
  * `org.openjdk.jmc:flightrecorder` (Leitura e agregação de eventos JFR)
  * `org.openjdk.jmc:flightrecorder.rules` (Motor de regras automatizadas)
  * `org.openjdk.jmc:flightrecorder.rules.jdk` (Implementação das heurísticas padrão do JMC)
* **Serialização:** Jackson (`ObjectMapper`)

## 2. Estrutura de Pacotes
```text
src/main/java/com/mcp/jmc/
├── JmcMcpApplication.java          # Entrypoint (desabilitar banner e logs no stdout)
├── config/
│   └── McpServerConfig.java        # Registro das Tools e schemas JSON
├── tools/
│   ├── ProcessControlTools.java    # Tools de listagem de PID e disparo de JFR
│   └── JfrAnalysisTools.java       # Tools de leitura e diagnóstico de arquivos .jfr
├── service/
│   ├── JcmdExecutorService.java    # Execução segura de comandos jcmd via ProcessBuilder
│   ├── JfrRuleEngineService.java   # Integração com RuleRegistry do JMC
│   └── JfrAggregatorService.java   # Agregadores customizados (CPU, GC, Locks, Exceptions)
├── model/
│   └── dto/                        # Records Java representando os retornos enxutos para IA
└── util/
    └── PathValidator.java          # Validação de segurança de arquivos e PIDs
```

## 3. Estratégia de Implementação

### Fase 1: Setup e Integração JMC
* Configurar Maven com os repositórios necessários (Maven Central / Adoptium / JMC artifacts).
* Garantir que o `application.properties` desative logs no console (`logging.pattern.console=`) caso use transporte `STDIO`.

### Fase 2: Controle de Processos (Tempo Real)
* Utilizar `com.sun.tools.attach.VirtualMachine.list()` (nativo do módulo `jdk.attach`) para listar JVMs ativas sem depender de parsing de texto do `jcmd -l`.
* Utilizar `ProcessBuilder` invocando `jcmd <pid> JFR.start ...` gravando em `/tmp/jmc-mcp-<pid>-<timestamp>.jfr`.

### Fase 3: Motor de Regras e Agregadores
* Carregar eventos usando `JfrLoaderToolkit.loadEvents(File)`.
* Executar `RuleRegistry.getRules()` de forma assíncrona (`RunnableFuture<IResult>`) filtrando resultados pelo score mínimo informado.
* Criar agregadores usando `IItemCollection` e `ItemFilters` do JMC para extrair estatísticas de `jdk.ExecutionSample`, `jdk.GarbageCollection`, `jdk.JavaMonitorEnter` e `jdk.JavaExceptionThrow`.

### Fase 4: Empacotamento
* Gerar um Fat JAR (`spring-boot-maven-plugin` ou `maven-shade-plugin`) para execução simples via `java -jar jmc-mcp-server.jar` nos clientes MCP.
