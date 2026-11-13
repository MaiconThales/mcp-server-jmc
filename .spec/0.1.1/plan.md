# Plano Técnico: Versão 0.1.1 (estrutura vigente)

## 1. Metadados do Projeto (`pom.xml`)

* **GroupId:** `io.github.maiconthales`
* **ArtifactId:** `mcp-server-jmc`
* **Version:** `0.1.1`
* **Dependência adicional:** `org.projectlombok:lombok`, com o processamento de anotações configurado no `maven-compiler-plugin`.

## 2. Estrutura Atual de Diretórios e Pacotes

A localização abaixo é a fonte de verdade para a versão `0.1.1`. Nenhuma etapa deste plano deve mover as classes para outro pacote.

```text
src/main/java/io/github/maiconthales/mcp_server_jmc/
├── McpServerJmcApplication.java
├── config/
│   └── McpServerConfig.java
├── component/
│   ├── cache/
│   │   └── JfrItemCollectionCache.java
│   ├── mcp/
│   │   └── McpStdioServer.java
│   └── tool/
│       ├── ProcessControlTools.java
│       └── JfrAnalysisTools.java
├── service/
│   ├── JcmdExecutorService.java
│   ├── JfrEventService.java
│   ├── JfrAggregatorService.java
│   └── JfrRuleEngineService.java
├── validator/
│   └── PathValidator.java
└── dto/
    ├── AllocatedClassDto.java
    ├── FrequentExceptionDto.java
    ├── GcMemorySummaryDto.java
    ├── HotMethodDto.java
    ├── JfrRecordingStatusDto.java
    ├── JvmProcessDto.java
    ├── LockContentionDto.java
    ├── RecordingResultDto.java
    └── RuleDiagnosticDto.java
```

## 3. Estratégia de Execução e Auditoria

### Etapa 1: Build e pacote base

Verificar a versão `0.1.1`, a dependência do Lombok e a compilação Maven sem alterar a localização das classes.

### Etapa 2: DTOs

Confirmar que os nove DTOs listados em `dto` são classes públicas independentes, usam somente `@Getter` e `@Setter` entre as anotações Lombok de acesso e possuem JavaDoc em português. Verificar também se não existem DTOs internos ou `record` remanescentes.

### Etapa 3: Componentes, serviços e validação

Auditar as classes nos pacotes onde elas realmente estão:

* `component.tool`, `component.mcp` e `component.cache` para os componentes Spring anotados com `@Component`;
* `service` para `JcmdExecutorService`, `JfrEventService`, `JfrAggregatorService` e `JfrRuleEngineService`, anotados com `@Service`;
* `validator` para `PathValidator`;
* `config` para `McpServerConfig`.

Verificar injeção por construtor/`@RequiredArgsConstructor`, nomenclatura, JavaDoc e imports, sem propor migração de pacote.

### Etapa 4: Limpeza e regressão

Executar a auditoria de testes e o `mvn clean verify`/`mvn test` quando aplicável. Confirmar STDIO, contratos das tools, validação de caminhos e PID e agregação limitada dos resultados. Esta etapa permanece pendente conforme `tasks.md`.

## 4. Restrições de Arquitetura

* Não mover classes da árvore vigente.
* Não exigir que `@Service` esteja em `component.*`; `service` é o pacote real e documentado dos serviços.
* Não exigir que `PathValidator` esteja em `component.validator`; sua localização real é `validator`.
* Não alterar contratos MCP nem escrever logs no `stdout`.
