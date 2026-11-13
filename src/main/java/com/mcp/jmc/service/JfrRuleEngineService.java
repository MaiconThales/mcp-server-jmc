package com.mcp.jmc.service;

import com.mcp.jmc.model.dto.Dtos;
import com.mcp.jmc.util.PathValidator;
import org.springframework.stereotype.Service;
import java.util.List;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.rules.RuleRegistry;
import org.openjdk.jmc.flightrecorder.rules.TypedResult;
import org.openjdk.jmc.flightrecorder.rules.util.RulesToolkit;

/** Executes the JMC rule registry and translates its results to the MCP DTO contract. */
@Service
public class JfrRuleEngineService {
    private final JfrAggregatorService aggregators;
    public JfrRuleEngineService(JfrAggregatorService aggregators) { this.aggregators = aggregators; }
    public List<Dtos.RuleDiagnosticDTO> analyze(String file, double minScore) throws Exception {
        if (minScore < 0 || minScore > 100) throw new IllegalArgumentException("minScore must be 0..100");
        var path = PathValidator.validateJfr(file);
        try {
            var collection = JfrLoaderToolkit.loadEvents(path.toFile());
            var evaluated = RulesToolkit.evaluateParallel(RuleRegistry.getRules(), collection,
                    org.openjdk.jmc.common.util.IPreferenceValueProvider.DEFAULT_VALUES,
                    Math.max(1, Runtime.getRuntime().availableProcessors()));
            return evaluated.values().stream().map(f -> {
                try { return f.get(); } catch (Exception e) { return null; }
            }).filter(r -> r != null).map(r -> {
                var score = r.getResult(TypedResult.SCORE);
                var numeric = score == null ? 0.0 : score.doubleValue();
                return new Dtos.RuleDiagnosticDTO(r.getRule().getName(), numeric,
                        r.getSeverity().getLocalizedName(), r.getSummary(), r.getSolution());
            }).filter(r -> r.score() >= minScore).toList();
        } catch (Exception e) {
            throw new IllegalStateException("JMC rule evaluation failed for " + path, e);
        }
    }
}
