package io.github.maiconthales.mcp_server_jmc.service;

import io.github.maiconthales.mcp_server_jmc.dto.AllocatedClassDto;
import io.github.maiconthales.mcp_server_jmc.dto.FrequentExceptionDto;
import io.github.maiconthales.mcp_server_jmc.dto.GcMemorySummaryDto;
import io.github.maiconthales.mcp_server_jmc.dto.HotMethodDto;
import io.github.maiconthales.mcp_server_jmc.dto.LockContentionDto;
import jdk.jfr.consumer.RecordedClass;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedMethod;
import jdk.jfr.consumer.RecordedStackTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Agrega eventos JFR em diagnósticos compactos para o contexto MCP. */
@Service
@RequiredArgsConstructor
public class JfrAggregatorService {
    private static final int MAX_LOCK_GROUPS = 50;
    private static final int MAX_TOP_N = 100;
    private final JfrEventService source;

    /** Agrega amostras de CPU por método no topo da stack.
     *
     * @param file caminho da gravação JFR
     * @param topN quantidade máxima de métodos
     * @return métodos ordenados por quantidade de amostras
     * @throws Exception se a leitura da gravação falhar
     */
    public List<HotMethodDto> cpu(String file, int topN) throws Exception {
        int limit = bounded(topN);
        var counts = new HashMap<String, Long>();
        for (RecordedEvent event : source.events(file)) {
            if (type(event, "jdk.ExecutionSample")) {
                counts.merge(topFrame(event), 1L, Long::sum);
            }
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream()
                .sorted(byCountThenName())
                .limit(limit)
                .map(entry -> new HotMethodDto(entry.getKey(), entry.getValue(),
                        total == 0 ? 0 : entry.getValue() * 100.0 / total))
                .toList();
    }

    /** Agrega pausas de GC, uso do heap e classes alocadas.
     *
     * @param file caminho da gravação JFR
     * @param topClasses quantidade máxima de classes
     * @return resumo de memória e coleta
     * @throws Exception se a leitura da gravação falhar
     */
    public GcMemorySummaryDto gc(String file, int topClasses) throws Exception {
        long count = 0;
        long total = 0;
        long max = 0;
        long before = 0;
        long after = 0;
        var allocations = new HashMap<String, long[]>();

        for (RecordedEvent event : source.events(file)) {
            if (type(event, "jdk.GarbageCollection")) {
                long milliseconds = durationMs(event);
                count++;
                total += milliseconds;
                max = Math.max(max, milliseconds);
            }
            if (type(event, "jdk.GCHeapSummary")) {
                String when = text(event, "when");
                long heap = value(event, "heapUsed");
                if (when != null && when.toLowerCase().contains("before")) {
                    before = heap;
                }
                if (when != null && when.toLowerCase().contains("after")) {
                    after = heap;
                }
            }
            if (type(event, "jdk.ObjectAllocationInNewTLAB")) {
                String className = className(event);
                long[] allocation = allocations.computeIfAbsent(className, ignored -> new long[2]);
                allocation[0] += value(event, "allocationSize");
                allocation[1]++;
            }
        }

        var top = allocations.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, long[]>>comparingLong(entry -> entry.getValue()[0])
                        .reversed().thenComparing(Map.Entry::getKey))
                .limit(bounded(topClasses))
                .map(entry -> new AllocatedClassDto(entry.getKey(), entry.getValue()[0],
                        entry.getValue()[1]))
                .toList();
        return new GcMemorySummaryDto(count, total, max, before, after, top);
    }

    /** Agrupa contenções de monitor por classe e limita o resultado.
     *
     * @param file caminho da gravação JFR
     * @param minMs duração mínima em milissegundos
     * @return contenções agrupadas
     * @throws Exception se a leitura da gravação falhar
     */
    public List<LockContentionDto> locks(String file, long minMs) throws Exception {
        var grouped = new HashMap<String, long[]>();
        var traces = new HashMap<String, String>();
        long minimum = Math.max(0, minMs);
        for (RecordedEvent event : source.events(file)) {
            long duration = durationMs(event);
            if (type(event, "jdk.JavaMonitorEnter") && duration >= minimum) {
                String className = className(event);
                long[] values = grouped.computeIfAbsent(className, ignored -> new long[2]);
                values[0] += duration;
                values[1]++;
                traces.putIfAbsent(className, stack(event));
            }
        }
        return grouped.entrySet().stream()
                .sorted(byCountThenName())
                .limit(MAX_LOCK_GROUPS)
                .map(entry -> new LockContentionDto(entry.getKey(), entry.getValue()[0],
                        entry.getValue()[1], traces.get(entry.getKey())))
                .toList();
    }

    /** Agrupa lançamentos de exceções por classe.
     *
     * @param file caminho da gravação JFR
     * @param topN quantidade máxima de exceções
     * @return exceções agrupadas
     * @throws Exception se a leitura da gravação falhar
     */
    public List<FrequentExceptionDto> exceptions(String file, int topN) throws Exception {
        var groups = new HashMap<String, Long>();
        var messages = new HashMap<String, String>();
        var traces = new HashMap<String, String>();
        for (RecordedEvent event : source.events(file)) {
            if (type(event, "jdk.JavaExceptionThrow")) {
                String className = className(event);
                groups.merge(className, 1L, Long::sum);
                String message = text(event, "message");
                if (message != null) {
                    messages.putIfAbsent(className, message);
                }
                traces.putIfAbsent(className, stack(event));
            }
        }
        return groups.entrySet().stream()
                .sorted(byCountThenName())
                .limit(bounded(topN))
                .map(entry -> new FrequentExceptionDto(entry.getKey(),
                        messages.getOrDefault(entry.getKey(), ""), entry.getValue(),
                        traces.get(entry.getKey())))
                .toList();
    }

    /** Limita uma quantidade ao intervalo seguro da API.
     *
     * @param value quantidade solicitada
     * @return quantidade entre um e cem
     */
    private static int bounded(int value) {
        return Math.max(1, Math.min(value, MAX_TOP_N));
    }

    /** Cria um comparador determinístico por contagem e nome.
     *
     * @param <T> tipo da entrada com nome e contagem
     * @return comparador de contagem decrescente e nome crescente
     */
    private static <T extends Map.Entry<String, ?>> Comparator<T> byCountThenName() {
        return Comparator.comparingLong((T entry) -> count(entry.getValue()))
                .reversed().thenComparing(Map.Entry::getKey);
    }

    /** Obtém a contagem armazenada em um valor agregado.
     *
     * @param value valor Long ou vetor de métricas
     * @return contagem ou duração agregada
     */
    private static long count(Object value) {
        return value instanceof long[] values ? values[0] : (Long) value;
    }

    /** Verifica o tipo de um evento JFR.
     *
     * @param event evento analisado
     * @param expectedType tipo esperado
     * @return verdadeiro quando os tipos coincidem
     */
    private static boolean type(RecordedEvent event, String expectedType) {
        return event.getEventType().getName().equals(expectedType);
    }

    /** Calcula a duração de um evento em milissegundos.
     *
     * @param event evento analisado
     * @return duração não negativa em milissegundos
     */
    private static long durationMs(RecordedEvent event) {
        return Math.max(0, Duration.between(event.getStartTime(), event.getEndTime()).toMillis());
    }

    /** Lê um campo numérico sem interromper a agregação por campos opcionais.
     *
     * @param event evento analisado
     * @param field nome do campo
     * @return valor do campo ou zero
     */
    private static long value(RecordedEvent event, String field) {
        try {
            return event.getLong(field);
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    /** Lê um campo textual opcional de um evento.
     *
     * @param event evento analisado
     * @param field nome do campo
     * @return texto do campo ou nulo
     */
    private static String text(RecordedEvent event, String field) {
        try {
            return event.getString(field);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /** Obtém o nome da classe associado às várias formas de evento.
     *
     * @param event evento analisado
     * @return nome da classe ou {@code unknown}
     */
    private static String className(RecordedEvent event) {
        for (String field : List.of("throwable", "monitorClass", "eventClass", "objectClass")) {
            try {
                Object value = event.getValue(field);
                if (value instanceof RecordedClass recordedClass) {
                    return recordedClass.getName();
                }
                if (value instanceof RecordedMethod recordedMethod) {
                    return recordedMethod.getType().getName();
                }
                if (value != null && !value.toString().isBlank()) {
                    return value.toString();
                }
            } catch (RuntimeException ignored) {
                // Campos opcionais variam entre versões do JDK.
            }
        }
        return "unknown";
    }

    /** Obtém a assinatura completa do primeiro frame da stack.
     *
     * @param event evento analisado
     * @return assinatura do frame ou {@code unknown}
     */
    private static String topFrame(RecordedEvent event) {
        var stackTrace = event.getStackTrace();
        if (stackTrace == null || stackTrace.getFrames().isEmpty()) {
            return "unknown";
        }
        var method = stackTrace.getFrames().getFirst().getMethod();
        return method.getType().getName() + "." + method.getName() + method.getDescriptor();
    }

    /** Formata até cinco frames representativos do evento.
     *
     * @param event evento analisado
     * @return stack formatada
     */
    private static String stack(RecordedEvent event) {
        RecordedStackTrace stackTrace = event.getStackTrace();
        return stackTrace == null ? "" : stackTrace.getFrames().stream()
                .limit(5)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
