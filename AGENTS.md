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
