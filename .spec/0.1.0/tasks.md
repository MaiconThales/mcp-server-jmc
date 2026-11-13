# Tasks de Desenvolvimento

## Épico 1: Configuração do Projeto
- [x] **T1.1:** Inicializar projeto Maven com Java 21.
- [x] **T1.2:** Adicionar dependências do Spring AI MCP Server (ou MCP Java SDK) no `pom.xml`.
- [x] **T1.3:** Adicionar dependências do OpenJDK Mission Control (`flightrecorder`, `flightrecorder.rules`, `flightrecorder.rules.jdk`).
- [x] **T1.4:** Configurar `application.yml` para modo `STDIO` limpo (sem poluição de logs no `stdout`).

## Épico 2: Coleta em Tempo Real (PID)
- [x] **T2.1:** Implementar `PathValidator` para validar existência, leitura e extensão `.jfr` dos arquivos, além de sanitizar PIDs.
- [x] **T2.2:** Criar `JcmdExecutorService.listJvms()` usando `VirtualMachine.list()` do pacote `jdk.attach`.
- [x] **T2.3:** Criar `JcmdExecutorService.startRecording()` executando `jcmd <pid> JFR.start` com suporte a duração e template (`default`/`profile`).
- [x] **T2.4:** Criar `JcmdExecutorService.checkRecording()` usando `jcmd <pid> JFR.check` para verificar conclusão da gravação.
- [x] **T2.5:** Expor `list_active_jvms`, `start_jfr_recording` e `check_jfr_recording` na classe `ProcessControlTools`.

## Épico 3: Motor de Análise JFR
- [x] **T3.1:** Implementar cache simples em memória (LRU) para `IItemCollection` baseado no hash/timestamp do arquivo `.jfr` (evita reabrir o mesmo arquivo várias vezes durante uma investigação da IA).
- [x] **T3.2:** Implementar `JfrRuleEngineService` para rodar todas as regras do `RuleRegistry` e converter `IResult` em DTOs limpos (`RuleDiagnosticDTO`).
- [x] **T3.3:** Implementar agregador de **CPU Hot Methods** (`jdk.ExecutionSample`) agrupando por método topo da pilha.
- [x] **T3.4:** Implementar agregador de **GC e Memória** (`jdk.GarbageCollection`, `jdk.GCHeapSummary`, `jdk.ObjectAllocationInNewTLAB`).
- [x] **T3.5:** Implementar agregador de **Lock Contention** (`jdk.JavaMonitorEnter`) com filtro de duração mínima e stack trace.
- [x] **T3.6:** Implementar agregador de **Exceções** (`jdk.JavaExceptionThrow`) agrupadas por classe de exceção.
- [x] **T3.7:** Expor as 5 tools de análise na classe `JfrAnalysisTools` com descrições detalhadas para orientar o LLM.

## Épico 4: Testes e Validação
- [x] **T4.1:** Criar testes unitários carregando um arquivo `sample.jfr` real e validando os DTOs de saída.
- [x] **T4.2:** Testar o ciclo completo em tempo real contra uma aplicação Java de teste (simulando vazamento de memória e deadlock).
- [x] **T4.3:** Validar integração com um cliente MCP (ex: Claude Desktop, Cursor ou MCP Inspector) via `STDIO`.
- [x] **T4.4:** Documentar no `README.md` como compilar o JAR e configurar o `mcpServers` JSON no cliente.
