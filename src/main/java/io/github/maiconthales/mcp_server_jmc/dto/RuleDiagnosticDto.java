package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Diagnóstico produzido por uma regra de análise JMC. */
@Getter
@Setter
public class RuleDiagnosticDto {
    /** Nome da regra que produziu o diagnóstico. */
    private String ruleName;

    /** Pontuação atribuída pela regra. */
    private double score;

    /** Severidade do diagnóstico. */
    private String severity;

    /** Resumo do problema identificado. */
    private String summary;

    /** Solução recomendada para o problema. */
    private String solution;

    /** Cria um diagnóstico de regra.
     * @param ruleName nome da regra
     * @param score pontuação obtida
     * @param severity severidade do diagnóstico
     * @param summary resumo do diagnóstico
     * @param solution solução recomendada
     */
    public RuleDiagnosticDto(String ruleName, double score, String severity, String summary, String solution) {
        this.ruleName = ruleName;
        this.score = score;
        this.severity = severity;
        this.summary = summary;
        this.solution = solution;
    }
}
