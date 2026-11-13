package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Representa um resumo das contenções agrupadas por classe de monitor.
 *
 * <p>Os métodos de acesso são gerados pelo Lombok por meio das anotações
 * {@link Getter} e {@link Setter}.</p>
 */
@Getter
@Setter
public class LockContentionDto {
    /** Classe do objeto monitor que sofreu contenção. */
    private String lockClass;

    /** Tempo total, em milissegundos, durante o qual as threads ficaram bloqueadas. */
    private long totalBlockedMs;

    /** Quantidade de ocorrências de contenção identificadas. */
    private long occurrences;

    /** Stack trace representativa das contenções agrupadas. */
    private String stackTrace;

    /**
     * Cria um resumo de contenção.
     *
     * @param lockClass classe do monitor
     * @param totalBlockedMs tempo total bloqueado, em milissegundos
     * @param occurrences quantidade de ocorrências
     * @param stackTrace stack representativa
     */
    public LockContentionDto(String lockClass, long totalBlockedMs, long occurrences, String stackTrace) {
        this.lockClass = lockClass;
        this.totalBlockedMs = totalBlockedMs;
        this.occurrences = occurrences;
        this.stackTrace = stackTrace;
    }
}
