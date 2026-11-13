# Especificação: mcp-server-jmc (Versão 0.1.1 - Padronização)

## 1. Contexto e Histórico

* **Versão `0.1.0` (Concluída):** Implementação funcional inicial das MCP Tools de controle de processo (`list_active_jvms`, `start_jfr_recording`, `check_jfr_recording`) e análise de arquivos `.jfr` (`analyze_jfr_automated_rules`, `get_cpu_hot_methods`, `analyze_gc_and_memory`, `find_lock_contentions`, `get_frequent_exceptions`).
* **Versão `0.1.1` (Atual):** Padronização estrutural, isolamento de modelos de dados, adoção controlada do Lombok e cobertura de JavaDoc em português, mantendo a compatibilidade funcional com os contratos MCP da `0.1.0`.

## 2. Requisitos de Refatoração e Padrões Obrigatórios

### 2.1. Hierarquia de Pacotes (árvore vigente)

Todo o código-fonte da aplicação deve residir sob o pacote raiz `io.github.maiconthales.mcp_server_jmc`. A localização vigente das classes deve ser preservada; esta especificação não exige mover classes já existentes.

* **`io.github.maiconthales.mcp_server_jmc.component.tool`**: `ProcessControlTools` e `JfrAnalysisTools`.
* **`io.github.maiconthales.mcp_server_jmc.component.mcp`**: `McpStdioServer`.
* **`io.github.maiconthales.mcp_server_jmc.component.cache`**: `JfrItemCollectionCache`.
* **`io.github.maiconthales.mcp_server_jmc.service`**: `JcmdExecutorService`, `JfrEventService`, `JfrAggregatorService` e `JfrRuleEngineService`. Essas classes permanecem neste pacote e são os serviços anotados com `@Service`.
* **`io.github.maiconthales.mcp_server_jmc.validator`**: `PathValidator`.
* **`io.github.maiconthales.mcp_server_jmc.config`**: `McpServerConfig`.
* **`io.github.maiconthales.mcp_server_jmc.dto`**: todos os DTOs de entrada e saída.

As classes anotadas com `@Component` devem permanecer dentro de `component.*`; as classes anotadas com `@Service` permanecem em `service`, conforme a árvore real. `PathValidator` permanece em `validator` e não deve ser migrado para `component.validator`.

### 2.2. Isolamento de DTOs

* Cada DTO deve ser uma classe pública independente em seu próprio arquivo `.java`.
* É proibido agrupar múltiplos DTOs no mesmo arquivo.
* É proibido declarar `record` ou classes DTO aninhadas dentro de Tools ou Services.

### 2.3. Uso Restrito do Lombok

* Manter a dependência do Project Lombok no `pom.xml`.
* Nos DTOs, utilizar exclusivamente `@Getter` e `@Setter` (com construtores explícitos ou as anotações de construtor necessárias ao framework).
* Nos componentes que possuem injeção de dependência, utilizar `@RequiredArgsConstructor` com atributos `private final`, quando aplicável ao código existente.
* É proibido o uso da anotação `@Data`.

### 2.4. Convenções de Nomenclatura

* **Classes e métodos:** PascalCase para classes e lowerCamelCase para métodos.
* **Variáveis, atributos e parâmetros:** lowerCamelCase.

### 2.5. Documentação (JavaDoc)

Todas as classes, interfaces, enums e métodos (públicos, protegidos e privados) devem possuir JavaDoc em português. O JavaDoc de métodos deve documentar o propósito, parâmetros, retorno e exceções quando aplicável.

## 3. Preservação de Requisitos da `0.1.0`

* **Transporte MCP:** Manter `STDIO` limpo, sem qualquer escrita no `stdout` fora do protocolo JSON-RPC.
* **Contratos das Tools:** Manter os mesmos nomes de ferramentas, nomes de parâmetros e estruturas JSON de retorno entregues na `0.1.0`.
* **Segurança de entrada:** Validar caminhos de arquivos `.jfr` com `PathValidator` e validar PID antes de repassá-lo ao `ProcessBuilder`.
* **Proteção de contexto:** Agregar, ordenar e limitar diagnósticos e eventos JFR; não retornar dumps brutos ou listas gigantes.

## 4. Critérios de Aceite da Versão 0.1.1

1. A árvore de pacotes documentada neste arquivo corresponde à árvore real em `src/main/java` e não requer migração de classes.
2. Todos os DTOs estão isolados em `dto`, um por arquivo, sem `record` ou DTO interno.
3. Componentes anotados com `@Component` estão em `component.*`; serviços anotados com `@Service` estão em `service`; `PathValidator` está em `validator`.
4. Não há uso de `@Data`, o JavaDoc está em português e a nomenclatura segue as convenções estabelecidas.
5. Os testes e a geração do artefato permanecem itens de auditoria pendentes até a conclusão das tasks do Épico 4.
