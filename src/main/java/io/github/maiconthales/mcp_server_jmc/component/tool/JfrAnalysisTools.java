package io.github.maiconthales.mcp_server_jmc.component.tool;

import io.github.maiconthales.mcp_server_jmc.service.JfrAggregatorService;
import io.github.maiconthales.mcp_server_jmc.service.JfrRuleEngineService;
import io.github.maiconthales.mcp_server_jmc.dto.FrequentExceptionDto;
import io.github.maiconthales.mcp_server_jmc.dto.GcMemorySummaryDto;
import io.github.maiconthales.mcp_server_jmc.dto.HotMethodDto;
import io.github.maiconthales.mcp_server_jmc.dto.LockContentionDto;
import io.github.maiconthales.mcp_server_jmc.dto.RuleDiagnosticDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Expõe ao MCP as análises agregadas e as regras JMC de uma gravação.
 *
 * <p>Os métodos Java seguem a convenção {@code lowerCamelCase}. Os nomes
 * externos das ferramentas MCP são definidos separadamente no registro do
 * servidor e preservam o contrato com sublinhados.</p>
 */
@Component
@RequiredArgsConstructor
public class JfrAnalysisTools {

    /** Serviço que agrega os eventos da gravação JFR. */
    private final JfrAggregatorService aggregator;

    /** Serviço que calcula os diagnósticos automatizados. */
    private final JfrRuleEngineService rules;

    /**
     * Executa as regras automatizadas JMC.
     *
     * @param filePath caminho da gravação
     * @param minScore pontuação mínima
     * @return diagnósticos encontrados
     * @throws Exception se a análise falhar
     */
    public List<RuleDiagnosticDto> analyzeJfrAutomatedRules(String filePath, double minScore)
            throws Exception {
        return rules.analyze(filePath, minScore);
    }

    /**
     * Executa regras usando a pontuação mínima padrão.
     *
     * @param filePath caminho da gravação
     * @return diagnósticos encontrados
     * @throws Exception se a análise falhar
     */
    public List<RuleDiagnosticDto> analyzeJfrAutomatedRules(String filePath) throws Exception {
        return analyzeJfrAutomatedRules(filePath, 50.0);
    }

    /**
     * Retorna os métodos mais quentes da gravação.
     *
     * @param filePath caminho da gravação
     * @param topN quantidade máxima de métodos
     * @return métodos quentes
     * @throws Exception se a análise falhar
     */
    public List<HotMethodDto> getCpuHotMethods(String filePath, int topN) throws Exception {
        return aggregator.cpu(filePath, topN);
    }

    /**
     * Retorna métodos quentes usando o limite padrão.
     *
     * @param filePath caminho da gravação
     * @return métodos quentes
     * @throws Exception se a análise falhar
     */
    public List<HotMethodDto> getCpuHotMethods(String filePath) throws Exception {
        return getCpuHotMethods(filePath, 10);
    }

    /**
     * Resume coleta de lixo e memória.
     *
     * @param filePath caminho da gravação
     * @param topClasses quantidade máxima de classes
     * @return resumo de memória
     * @throws Exception se a análise falhar
     */
    public GcMemorySummaryDto analyzeGcAndMemory(String filePath, int topClasses) throws Exception {
        return aggregator.gc(filePath, topClasses);
    }

    /**
     * Resume coleta e memória usando o limite padrão.
     *
     * @param filePath caminho da gravação
     * @return resumo de memória
     * @throws Exception se a análise falhar
     */
    public GcMemorySummaryDto analyzeGcAndMemory(String filePath) throws Exception {
        return analyzeGcAndMemory(filePath, 10);
    }

    /**
     * Localiza contenções de monitor.
     *
     * @param filePath caminho da gravação
     * @param minDurationMs duração mínima em milissegundos
     * @return contenções encontradas
     * @throws Exception se a análise falhar
     */
    public List<LockContentionDto> findLockContentions(String filePath, long minDurationMs)
            throws Exception {
        return aggregator.locks(filePath, minDurationMs);
    }

    /**
     * Localiza contenções usando o limite padrão.
     *
     * @param filePath caminho da gravação
     * @return contenções encontradas
     * @throws Exception se a análise falhar
     */
    public List<LockContentionDto> findLockContentions(String filePath) throws Exception {
        return findLockContentions(filePath, 50);
    }

    /**
     * Retorna as exceções mais frequentes.
     *
     * @param filePath caminho da gravação
     * @param topN quantidade máxima de exceções
     * @return exceções encontradas
     * @throws Exception se a análise falhar
     */
    public List<FrequentExceptionDto> getFrequentExceptions(String filePath, int topN)
            throws Exception {
        return aggregator.exceptions(filePath, topN);
    }

    /**
     * Retorna exceções frequentes usando o limite padrão.
     *
     * @param filePath caminho da gravação
     * @return exceções encontradas
     * @throws Exception se a análise falhar
     */
    public List<FrequentExceptionDto> getFrequentExceptions(String filePath) throws Exception {
        return getFrequentExceptions(filePath, 10);
    }
}
