package io.github.maiconthales.mcp_server_jmc.component.mcp;

import io.github.maiconthales.mcp_server_jmc.component.tool.JfrAnalysisTools;
import io.github.maiconthales.mcp_server_jmc.component.tool.ProcessControlTools;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.concurrent.Callable;

/** Servidor JSON-RPC MCP sobre STDIO; o SDK controla o stdout da aplicação. */
@Component
@RequiredArgsConstructor
public final class McpStdioServer {
    private final ProcessControlTools process;
    private final JfrAnalysisTools analysis;
    private McpSyncServer server;

    /** Inicializa o transporte STDIO e registra as ferramentas MCP. */
    @PostConstruct
    void start() {
        var transport = new StdioServerTransportProvider(McpJsonDefaults.getMapper());
        server = McpServer.sync(transport)
                .serverInfo("mcp-server-jmc", "0.1.1")
                .instructions("JVM/JFR diagnostics. File paths must point to readable .jfr files. "
                        + "Results are aggregated and bounded for safe model context use.")
                .immediateExecution(true)
                .tools(toolSpecifications())
                .build();
    }

    /** Fecha o servidor MCP durante o encerramento do contexto Spring. */
    @PreDestroy
    void stop() { if (server != null) server.closeGracefully(); }

    /** Constrói as especificações públicas das ferramentas MCP.
     * @return especificações das ferramentas disponíveis
     */
    private List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        var result = new ArrayList<McpServerFeatures.SyncToolSpecification>();
        result.add(tool("list_active_jvms", "List attachable JVMs with PID, main class and arguments.",
                schema(List.of(), Map.of()), (x, r) -> ok(Map.of("jvms", process.listActiveJvms()))));
        result.add(tool("start_jfr_recording", "Start a bounded JFR recording in the target JVM using the default or profile template.",
                schema(List.of("pid"), props("pid", integer(), "durationSeconds", integer(), "profile", string())),
                (x, r) -> safe(() -> process.startJfrRecording(integer(r, "pid"), integerOr(r, "durationSeconds", 30), stringOr(r, "profile", "profile")))));
        result.add(tool("check_jfr_recording", "Check a named recording and, by default, stop it and dump the complete .jfr file immediately.",
                schema(List.of("pid", "recordingName"), props("pid", integer(), "recordingName", string(), "forceDump", bool())),
                (x, r) -> safe(() -> process.checkJfrRecording(integer(r, "pid"), string(r, "recordingName"), boolOr(r, "forceDump", true)))));
        result.add(tool("analyze_jfr_automated_rules", "Run the JMC RuleRegistry rules against a recording and return only diagnostics meeting minScore.",
                schema(List.of("filePath"), props("filePath", string(), "minScore", number())),
                (x, r) -> safe(() -> Map.of("diagnostics", analysis.analyzeJfrAutomatedRules(string(r, "filePath"), numberOr(r, "minScore", 50.0))))));
        result.add(tool("get_cpu_hot_methods", "Aggregate ExecutionSample events by the complete top stack-frame method signature.",
                schema(List.of("filePath"), props("filePath", string(), "topN", integer())),
                (x, r) -> safe(() -> Map.of("methods", analysis.getCpuHotMethods(string(r, "filePath"), integerOr(r, "topN", 10))))));
        result.add(tool("analyze_gc_and_memory", "Summarize GC pauses, before/after heap usage, and bounded allocation classes.",
                schema(List.of("filePath"), props("filePath", string(), "topClasses", integer())),
                (x, r) -> safe(() -> analysis.analyzeGcAndMemory(string(r, "filePath"), integerOr(r, "topClasses", 10)))));
        result.add(tool("find_lock_contentions", "Find Java monitor contention above the requested duration, grouped by lock class.",
                schema(List.of("filePath"), props("filePath", string(), "minDurationMs", integer())),
                (x, r) -> safe(() -> Map.of("contentions", analysis.findLockContentions(string(r, "filePath"), integerOr(r, "minDurationMs", 50))))));
        result.add(tool("get_frequent_exceptions", "Aggregate JavaExceptionThrow events by robust exception class name and include a representative stack.",
                schema(List.of("filePath"), props("filePath", string(), "topN", integer())),
                (x, r) -> safe(() -> Map.of("exceptions", analysis.getFrequentExceptions(string(r, "filePath"), integerOr(r, "topN", 10))))));
        return result;
    }

    /** Cria uma especificação de ferramenta com seu manipulador síncrono.
     * @param name nome público da ferramenta
     * @param description descrição apresentada ao cliente MCP
     * @param input esquema de entrada da ferramenta
     * @param handler manipulador síncrono da chamada
     * @return especificação configurada
     */
    private static McpServerFeatures.SyncToolSpecification tool(String name, String description,
            Map<String, Object> input, BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange,
            McpSchema.CallToolRequest, McpSchema.CallToolResult> handler) {
        return new McpServerFeatures.SyncToolSpecification(McpSchema.Tool.builder(name)
                .description(description).inputSchema(input).build(), handler);
    }

    /** Converte um resultado de domínio em resposta MCP bem-sucedida.
     * @param value valor produzido pela operação
     * @return resposta MCP sem erro
     */
    private static McpSchema.CallToolResult ok(Object value) {
        return McpSchema.CallToolResult.builder().structuredContent(value).addTextContent(String.valueOf(value)).build();
    }
    /** Executa uma operação e converte falhas em respostas MCP de erro.
     * @param operation operação a executar
     * @return resposta MCP de sucesso ou erro
     */
    private static McpSchema.CallToolResult safe(Callable<?> operation) {
        try { return ok(operation.call()); }
        catch (Exception e) { return McpSchema.CallToolResult.builder().isError(true).addTextContent(String.valueOf(e.getMessage())).build(); }
    }
    /** Cria um schema JSON de entrada fechado e tipado.
     * @param required nomes obrigatórios
     * @param properties propriedades aceitas
     * @return esquema JSON
     */
    private static Map<String, Object> schema(List<String> required, Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties, "required", required, "additionalProperties", false);
    }
    /** Monta um mapa de propriedades a partir de pares nome/valor.
     * @param values pares de nome e esquema
     * @return mapa de propriedades
     */
    private static Map<String, Object> props(Object... values) {
        var result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) result.put((String) values[i], values[i + 1]);
        return result;
    }
    /** Retorna o schema JSON de texto.
     * @return esquema textual
     */
    private static Map<String, Object> string() { return Map.of("type", "string"); }
    /** Retorna o schema JSON de inteiro.
     * @return esquema inteiro
     */
    private static Map<String, Object> integer() { return Map.of("type", "integer"); }
    /** Retorna o schema JSON numérico.
     * @return esquema numérico
     */
    private static Map<String, Object> number() { return Map.of("type", "number"); }
    /** Retorna o schema JSON booleano.
     * @return esquema booleano
     */
    private static Map<String, Object> bool() { return Map.of("type", "boolean"); }
    /** Obtém os argumentos, substituindo nulo por mapa vazio.
     * @param r requisição MCP
     * @return argumentos da requisição
     */
    private static Map<String, Object> args(McpSchema.CallToolRequest r) { return r.arguments() == null ? Map.of() : r.arguments(); }
    /** Obtém um argumento textual obrigatório.
     * @param r requisição MCP
     * @param key nome do argumento
     * @return valor textual
     */
    private static String string(McpSchema.CallToolRequest r, String key) { return String.valueOf(args(r).get(key)); }
    /** Obtém um argumento textual opcional.
     * @param r requisição MCP
     * @param key nome do argumento
     * @param fallback valor padrão
     * @return valor textual
     */
    private static String stringOr(McpSchema.CallToolRequest r, String key, String fallback) { return args(r).containsKey(key) ? string(r, key) : fallback; }
    /** Obtém um argumento inteiro obrigatório.
     * @param r requisição MCP
     * @param key nome do argumento
     * @return valor inteiro
     */
    private static int integer(McpSchema.CallToolRequest r, String key) { return ((Number) args(r).get(key)).intValue(); }
    /** Obtém um inteiro opcional com valor padrão.
     * @param r requisição MCP
     * @param key nome do argumento
     * @param fallback valor padrão
     * @return valor inteiro
     */
    private static int integerOr(McpSchema.CallToolRequest r, String key, int fallback) { return args(r).containsKey(key) ? integer(r, key) : fallback; }
    /** Obtém um número opcional com valor padrão.
     * @param r requisição MCP
     * @param key nome do argumento
     * @param fallback valor padrão
     * @return valor numérico
     */
    private static double numberOr(McpSchema.CallToolRequest r, String key, double fallback) { return args(r).containsKey(key) ? ((Number) args(r).get(key)).doubleValue() : fallback; }
    /** Obtém um booleano opcional com valor padrão.
     * @param r requisição MCP
     * @param key nome do argumento
     * @param fallback valor padrão
     * @return valor booleano
     */
    private static boolean boolOr(McpSchema.CallToolRequest r, String key, boolean fallback) { return args(r).containsKey(key) ? (Boolean) args(r).get(key) : fallback; }
}


