# AGENTS.md - mcp-server-jmc

## Visão Geral do Projeto
Este repositório contém o **`mcp-server-jmc`**, um servidor Model Context Protocol (MCP) desenvolvido em **Java 21** que expõe diagnósticos de performance da JVM (via JDK Mission Control e Java Flight Recorder) para modelos de linguagem.

## Documentos de Referência (SDD)
Todo agente deve consultar e respeitar a fonte da verdade contida em:
* `docs/spec.md`: Requisitos funcionais, assinaturas das MCP Tools e regras não funcionais.
* `docs/plan.md`: Arquitetura de pacotes, stack tecnológica e decisões técnicas.
* `docs/tasks.md`: Backlog sequencial de tarefas.

## Regras Invioláveis de Arquitetura
1. **Proteção do `stdout`:** O transporte padrão do MCP é `STDIO` via JSON-RPC. **NUNCA** utilize `System.out.println` ou configure appenders de log no `stdout`. Todo log deve ir exclusivamente para `stderr` ou arquivo.
2. **Proteção de Contexto da IA:** Nunca retorne dumps brutos ou listas gigantes de eventos JFR nas respostas das Tools. Sempre agregue, ordene e limite os resultados (ex: Top N, filtro por score mínimo).
3. **Segurança de Entrada:** Todo caminho de arquivo (`filePath`) recebido pelas tools deve passar pelo `PathValidator` (garantindo extensão `.jfr` e bloqueando Path Traversal) e todo `pid` deve ser validado antes de ser repassado ao `ProcessBuilder`.
4. **Padrões de Código:** Utilize Java 21 moderno (`record` para DTOs, `var` quando legível, `try-with-resources` para I/O) e mantenha os métodos das Tools pequenos, delegando a lógica pesada para a camada `service`.

# AGENTS.md - mcp-server-jmc (v0.1.1)

## Visão Geral
Você está trabalhando na **versão `0.1.1`** do `mcp-server-jmc`, cujo objetivo é refatorar e padronizar a base funcional entregue na `0.1.0` sem quebrar nenhum contrato existente das MCP Tools.

## Documentos de Referência
* `docs/spec.md`: Regras de refatoração da `0.1.1` e preservação dos contratos da `0.1.0`.
* `docs/plan.md`: Mapeamento da estrutura de pacotes `io.github.maiconthales.mcp_server_jmc`.
* `docs/tasks.md`: Lista sequencial de tarefas da `0.1.1`.

## Checklist Rígido de Conformidade (Bloqueante para o `validator`)
1. **Pacote Base:** Todo código deve estar em `io.github.maiconthales.mcp_server_jmc`.
2. **Localização de Componentes:** Toda classe anotada com `@Component` (ou `@Service`) **deve** estar dentro de `io.github.maiconthales.mcp_server_jmc.component.*`.
3. **Isolamento de DTOs:**
   * Todo DTO deve estar em `io.github.maiconthales.mcp_server_jmc.dto`.
   * Cada DTO deve ser uma **classe separada em seu próprio arquivo `.java`**.
   * **Proibido** usar `record` ou classes DTO internas dentro de outras classes.
4. **Regras do Lombok:**
   * Usar **apenas** `@Getter` e `@Setter` nos DTOs.
   * Usar `@RequiredArgsConstructor` nos componentes para injeção de dependência.
   * **Proibido** usar `@Data`.
5. **Nomenclatura:** `CamelCase` para classes e métodos; `lowerCamelCase` para variáveis, parâmetros e atributos.
6. **JavaDoc Obrigatório:** 100% das classes e métodos (inclusive privados) devem possuir JavaDoc escrito em **português**.
7. **Isolamento de `stdout`:** Proibido qualquer uso de `System.out` ou logs no console padrão.
