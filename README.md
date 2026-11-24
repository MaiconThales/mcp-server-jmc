# JMC Diagnostic MCP Server

[![M8ven Score](https://m8ven.ai/badge/mcp/maiconthales-mcp-server-jmc)](https://m8ven.ai/mcp/maiconthales-mcp-server-jmc)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


Servidor Java 21 para diagnóstico de gravações Java Flight Recorder. O núcleo usa
`RecordingFile` (API estável do JDK) para permanecer compatível com versões diferentes
do JMC; os módulos `flightrecorder*` e o SDK oficial MCP ficam disponíveis para
integrações e extensões. Todas as respostas são agregadas e limitadas.

## Compilar

```bash
./mvnw test
./mvnw package
java -jar target/mcp-server-jmc-0.0.1-SNAPSHOT.jar
```

## Tools

`ProcessControlTools` expõe `list_active_jvms`, `start_jfr_recording` e
`check_jfr_recording`. `JfrAnalysisTools` expõe `analyze_jfr_automated_rules`,
`get_cpu_hot_methods`, `analyze_gc_and_memory`, `find_lock_contentions` e
`get_frequent_exceptions`. Os componentes podem ser registrados por qualquer
adaptador MCP compatível com o SDK oficial; a aplicação não escreve logs em stdout.

Exemplo de configuração de cliente:

```json
{
  "mcpServers": {
    "jmc": {
      "command": "java",
      "args": ["-jar", "C:/caminho/mcp-server-jmc-0.0.1-SNAPSHOT.jar"]
    }
  }
}
```

`filePath` precisa apontar para um arquivo `.jfr` legível. PIDs, duração (1–300 s),
templates (`default`/`profile`) e nomes de gravação são validados antes de chamar
`jcmd`. A cache LRU mantém quatro arquivos por chave de tamanho/data de modificação.

## Limitação conhecida

As assinaturas internas do `RuleRegistry` do JMC variam entre releases. Por isso a
fachada de regras fornece diagnósticos determinísticos baseados nos agregadores,
sem depender de classes internas voláteis; o SDK MCP está declarado no Maven para o
adaptador de transporte escolhido pelo cliente.
