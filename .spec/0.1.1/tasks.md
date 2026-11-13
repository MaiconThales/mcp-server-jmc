# Tasks de Desenvolvimento: Release 0.1.1

## Épico 1: Build e Estrutura de Pacotes (`0.1.1`)

- [x] **T1.1:** Atualizar `pom.xml` para `groupId` `io.github.maiconthales`, `artifactId` `mcp-server-jmc`, `version` `0.1.1` e adicionar suporte ao Lombok no `maven-compiler-plugin`.
- [x] **T1.2:** Manter `McpServerJmcApplication` no pacote raiz `io.github.maiconthales.mcp_server_jmc` e `McpServerConfig` em `io.github.maiconthales.mcp_server_jmc.config`, com JavaDoc em português nas classes e métodos.

## Épico 2: Extração e Padronização dos DTOs (`dto.*`)

- [x] **T2.1:** Extrair `JvmProcessDto` e `JfrRecordingStatusDto` para arquivos `.java` individuais em `io.github.maiconthales.mcp_server_jmc.dto`, usando apenas `@Getter` e `@Setter` e JavaDoc em português.
- [x] **T2.2:** Extrair `RuleDiagnosticDto` e `HotMethodDto` para arquivos `.java` individuais em `io.github.maiconthales.mcp_server_jmc.dto`, usando apenas `@Getter` e `@Setter` e JavaDoc em português.
- [x] **T2.3:** Extrair `GcMemorySummaryDto` e `AllocatedClassDto` para arquivos `.java` individuais em `io.github.maiconthales.mcp_server_jmc.dto`, usando apenas `@Getter` e `@Setter` e JavaDoc em português.
- [x] **T2.4:** Extrair `LockContentionDto` e `FrequentExceptionDto` para arquivos `.java` individuais em `io.github.maiconthales.mcp_server_jmc.dto`, usando apenas `@Getter` e `@Setter` e JavaDoc em português.
- [x] **T2.5:** Verificar se restou algum `record` ou classe DTO aninhada dentro de outras classes e removê-los.
- [x] **T2.6:** Manter `RecordingResultDto` isolado em seu próprio arquivo dentro de `io.github.maiconthales.mcp_server_jmc.dto`.

## Épico 3: Padronização dos Componentes e Serviços

- [x] **T3.1:** Padronizar `JfrItemCollectionCache` em `component.cache` e manter `PathValidator` em `io.github.maiconthales.mcp_server_jmc.validator`, aplicando JavaDoc em português sem migrar classes.
- [x] **T3.2:** Padronizar `JcmdExecutorService`, `JfrEventService`, `JfrRuleEngineService` e `JfrAggregatorService` em `io.github.maiconthales.mcp_server_jmc.service`, aplicando `@RequiredArgsConstructor` quando aplicável e JavaDoc em português em todos os métodos.
- [x] **T3.3:** Manter `ProcessControlTools` e `JfrAnalysisTools` em `component.tool` e `McpStdioServer` em `component.mcp`, aplicando `@RequiredArgsConstructor` quando aplicável, referenciando os DTOs e adicionando JavaDoc em português.

## Épico 4: Auditoria de Padrões e Testes (`0.1.1`)

- [x] **T4.1:** Atualizar a estrutura de pacotes de `src/test/java` para `io.github.maiconthales.mcp_server_jmc`, se necessário, e rodar `mvn test`.
- [x] **T4.2:** Auditar o código-fonte para garantir: (a) zero ocorrências de `@Data`, (b) zero DTOs internos/agrupados, (c) todas as classes `@Component` dentro de `component.*`, (d) classes `@Service` mantidas em `service`, (e) `PathValidator` mantido em `validator`, (f) JavaDoc em português em 100% das classes/métodos e (g) nomenclatura `CamelCase` / `lowerCamelCase`.
- [x] **T4.3:** Gerar o artefato `mcp-server-jmc-0.1.1.jar` via `mvn clean package` e validar a execução via `STDIO`.
