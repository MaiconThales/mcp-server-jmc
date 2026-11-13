# Especificação: JMC Diagnostic MCP Server

## 1. Visão Geral
O **JMC Diagnostic MCP Server** é um servidor Model Context Protocol (MCP) escrito em Java que expõe as capacidades de diagnóstico e análise do JDK Mission Control (JMC) para assistentes de IA. Ele permite inspecionar aplicações Java tanto a partir de gravações `.jfr` (Java Flight Recorder) já existentes quanto iniciando gravações sob demanda em processos JVM ativos (via PID).

## 2. Objetivos
* Permitir que LLMs diagnostiquem gargalos de CPU, vazamentos de memória, contenção de threads, exceções silenciosas e lentidão de I/O.
* Reaproveitar o motor de regras nativo do JMC (`RuleRegistry`) para entregar diagnósticos pré-processados com pontuação de severidade.
* Evitar o estouro de janela de contexto da IA retornando apenas dados agregados, filtrados e sumarizados em JSON.

## 3. Requisitos Funcionais (MCP Tools)

### 3.1. Gestão de Processos e Gravação (Tempo Real)
* **`list_active_jvms`**
  * **Descrição:** Lista todas as JVMs em execução no host atual.
  * **Parâmetros:** Nenhum.
  * **Retorno:** Lista contendo `pid`, `mainClass` e argumentos principais.
* **`start_jfr_recording`**
  * **Descrição:** Inicia uma gravação JFR em uma JVM alvo usando `jcmd` ou Attach API.
  * **Parâmetros:** `pid` (int, obrigatório), `durationSeconds` (int, padrão: 30, máx: 300), `profile` (string: `"default"` ou `"profile"`, padrão: `"profile"`).
  * **Retorno:** Caminho do arquivo `.jfr` temporário gerado, status da gravação e `recordingId`.
* **`check_jfr_recording`**
  * **Descrição:** Verifica o status de uma gravação em andamento ou força o dump imediato.
  * **Parâmetros:** `pid` (int, obrigatório), `recordingName` (string, obrigatório).
  * **Retorno:** Status (`RUNNING`, `STOPPED`) e caminho do arquivo disponível para análise.

### 3.2. Análise de Arquivos `.jfr`
* **`analyze_jfr_automated_rules`**
  * **Descrição:** Executa o motor de heurísticas do JMC sobre o arquivo `.jfr`.
  * **Parâmetros:** `filePath` (string, obrigatório), `minScore` (double, padrão: 50.0).
  * **Retorno:** Lista de problemas detectados contendo `ruleName`, `score` (0-100), `severity`, `summary` e `solution`.
* **`get_cpu_hot_methods`**
  * **Descrição:** Agrupa eventos `jdk.ExecutionSample` para identificar os métodos que mais consomem CPU.
  * **Parâmetros:** `filePath` (string, obrigatório), `topN` (int, padrão: 10).
  * **Retorno:** Ranking de métodos com assinatura completa, contagem de amostras e percentual estimado de CPU.
* **`analyze_gc_and_memory`**
  * **Descrição:** Avalia pausas de Garbage Collection e alocação de objetos.
  * **Parâmetros:** `filePath` (string, obrigatório), `topClasses` (int, padrão: 10).
  * **Retorno:** Tempo total e máximo de pausa (Stop-The-World), uso de heap pré/pós GC e top classes alocadas.
* **`find_lock_contentions`**
  * **Descrição:** Identifica monitores e blocos `synchronized` com alto tempo de espera.
  * **Parâmetros:** `filePath` (string, obrigatório), `minDurationMs` (long, padrão: 50).
  * **Retorno:** Lista de classes de lock, tempo total bloqueado, contagem de ocorrências e stack trace principal.
* **`get_frequent_exceptions`**
  * **Descrição:** Contabiliza exceções lançadas (`jdk.JavaExceptionThrow`), incluindo as capturadas em `try-catch`.
  * **Parâmetros:** `filePath` (string, obrigatório), `topN` (int, padrão: 10).
  * **Retorno:** Classe da exceção, mensagem mais comum, total de ocorrências e stack trace de origem.

## 4. Requisitos Não Funcionais
* **Transporte MCP:** Suporte primário a `STDIO` (para integração local com IDEs/Claude Desktop) e suporte opcional a `SSE/HTTP`.
* **Isolamento de Logs:** Todo log interno do servidor deve ser direcionado para `stderr` ou arquivo, nunca para `stdout` (reservado ao protocolo JSON-RPC do MCP).
* **Segurança:** Validação rigorosa de caminhos de arquivo (`filePath`) para evitar Path Traversal e restrição de leitura apenas a arquivos `.jfr`.
* **Performance:** O processamento de arquivos `.jfr` de até 200 MB deve ocorrer em menos de 5 segundos.