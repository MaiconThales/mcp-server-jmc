package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Representa um método que aparece com frequência em amostras de CPU. */
@Getter
@Setter
public class HotMethodDto {
    /** Assinatura do método identificado nas amostras. */
    private String method;

    /** Quantidade de amostras atribuídas ao método. */
    private long samples;

    /** Percentual de CPU representado pelas amostras do método. */
    private double cpuPercent;

    /** Cria um diagnóstico de método quente.
     * @param method assinatura do método
     * @param samples quantidade de amostras
     * @param cpuPercent percentual de CPU
     */
    public HotMethodDto(String method, long samples, double cpuPercent) {
        this.method = method;
        this.samples = samples;
        this.cpuPercent = cpuPercent;
    }
}
