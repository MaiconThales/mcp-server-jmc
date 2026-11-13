package io.github.maiconthales.mcp_server_jmc.service;

import io.github.maiconthales.mcp_server_jmc.dto.FrequentExceptionDto;
import io.github.maiconthales.mcp_server_jmc.dto.GcMemorySummaryDto;
import io.github.maiconthales.mcp_server_jmc.dto.HotMethodDto;
import io.github.maiconthales.mcp_server_jmc.dto.LockContentionDto;
import io.github.maiconthales.mcp_server_jmc.dto.RuleDiagnosticDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Produz diagnósticos determinísticos a partir dos agregadores de eventos JFR. */
@Service
@RequiredArgsConstructor
public class JfrRuleEngineService {
    private static final int MAX_DIAGNOSTICS = 50;
    private final JfrAggregatorService aggregators;

    /** Avalia indicadores agregados e retorna somente diagnósticos acima do limiar.
     *
     * @param file caminho da gravação JFR
     * @param minScore pontuação mínima aceita, entre zero e cem
     * @return diagnósticos ordenados por pontuação decrescente
     * @throws Exception se a leitura da gravação falhar
     */
    public List<RuleDiagnosticDto> analyze(String file, double minScore) throws Exception {
        validateScore(minScore);
        var diagnostics = new ArrayList<RuleDiagnosticDto>();
        addCpuDiagnostic(diagnostics, aggregators.cpu(file, 10));
        addGcDiagnostic(diagnostics, aggregators.gc(file, 10));
        addLockDiagnostics(diagnostics, aggregators.locks(file, 50));
        addExceptionDiagnostics(diagnostics, aggregators.exceptions(file, 10));
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.getScore() >= minScore)
                .sorted(Comparator.comparingDouble(RuleDiagnosticDto::getScore).reversed()
                        .thenComparing(RuleDiagnosticDto::getRuleName))
                .limit(MAX_DIAGNOSTICS)
                .toList();
    }

    /** Adiciona um diagnóstico quando a concentração de CPU for relevante.
     *
     * @param diagnostics lista de diagnósticos acumulados
     * @param methods métodos mais amostrados
     */
    private void addCpuDiagnostic(List<RuleDiagnosticDto> diagnostics, List<HotMethodDto> methods) {
        if (methods.isEmpty()) {
            return;
        }
        HotMethodDto method = methods.getFirst();
        double score = boundedScore(method.getCpuPercent() * 2.0);
        diagnostics.add(new RuleDiagnosticDto("cpu-hot-method", score, severity(score),
                "Amostras de CPU concentradas em " + method.getMethod(),
                "Investigue o método mais amostrado e sua carga de CPU."));
    }

    /** Adiciona um diagnóstico quando houver pausas de coleta de lixo.
     *
     * @param diagnostics lista de diagnósticos acumulados
     * @param summary resumo de memória e coleta
     */
    private void addGcDiagnostic(List<RuleDiagnosticDto> diagnostics, GcMemorySummaryDto summary) {
        if (summary.getGcCount() == 0) {
            return;
        }
        double score = boundedScore(summary.getMaxPauseMs() / 10.0);
        diagnostics.add(new RuleDiagnosticDto("gc-pause", score, severity(score),
                "Foram observadas pausas de coleta de lixo",
                "Verifique a duração máxima e a pressão de memória da JVM."));
    }

    /** Adiciona diagnósticos para as classes com maior contenção de monitor.
     *
     * @param diagnostics lista de diagnósticos acumulados
     * @param contentions contenções já agregadas e limitadas
     */
    private void addLockDiagnostics(List<RuleDiagnosticDto> diagnostics,
                                    List<LockContentionDto> contentions) {
        contentions.stream().limit(10).forEach(contention -> {
            double score = boundedScore(contention.getTotalBlockedMs() / 10.0);
            diagnostics.add(new RuleDiagnosticDto("lock-contention:" + contention.getLockClass(),
                    score, severity(score), "Contenção de monitor em " + contention.getLockClass(),
                    "Analise a stack trace e reduza a disputa pelo monitor."));
        });
    }

    /** Adiciona diagnósticos para classes que lançaram exceções frequentemente.
     *
     * @param diagnostics lista de diagnósticos acumulados
     * @param exceptions exceções já agregadas e limitadas
     */
    private void addExceptionDiagnostics(List<RuleDiagnosticDto> diagnostics,
                                         List<FrequentExceptionDto> exceptions) {
        exceptions.stream().limit(10).forEach(exception -> {
            double score = boundedScore(exception.getOccurrences());
            diagnostics.add(new RuleDiagnosticDto("frequent-exception:" + exception.getExceptionClass(),
                    score, severity(score), "Exceções frequentes de " + exception.getExceptionClass(),
                    "Investigue a causa e evite usar exceções como fluxo normal."));
        });
    }

    /** Valida a pontuação mínima recebida pela ferramenta.
     *
     * @param minScore pontuação mínima
     */
    private static void validateScore(double minScore) {
        if (Double.isNaN(minScore) || minScore < 0 || minScore > 100) {
            throw new IllegalArgumentException("minScore must be 0..100");
        }
    }

    /** Limita uma pontuação calculada ao intervalo do contrato.
     *
     * @param value pontuação calculada
     * @return pontuação entre zero e cem
     */
    private static double boundedScore(double value) {
        return Math.max(0, Math.min(100, value));
    }

    /** Traduz uma pontuação para a severidade exibida no diagnóstico.
     *
     * @param score pontuação do diagnóstico
     * @return severidade textual
     */
    private static String severity(double score) {
        return score >= 80 ? "HIGH" : score >= 50 ? "MEDIUM" : "LOW";
    }
}
