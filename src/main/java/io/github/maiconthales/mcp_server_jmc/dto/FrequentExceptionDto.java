package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Representa um resumo das exceções frequentes e de uma stack trace
 * representativa.
 *
 * <p>Os métodos de acesso são gerados pelo Lombok por meio das anotações
 * {@link Getter} e {@link Setter}.</p>
 */
@Getter
@Setter
public class FrequentExceptionDto {
    /** Nome completo da classe da exceção. */
    private String exceptionClass;

    /** Mensagem mais comum associada à exceção. */
    private String commonMessage;

    /** Quantidade de ocorrências da exceção. */
    private long occurrences;

    /** Stack trace representativa das ocorrências agrupadas. */
    private String stackTrace;

    /**
     * Cria um resumo de exceções.
     *
     * @param exceptionClass classe da exceção
     * @param commonMessage mensagem representativa
     * @param occurrences quantidade de ocorrências
     * @param stackTrace stack representativa
     */
    public FrequentExceptionDto(String exceptionClass, String commonMessage, long occurrences, String stackTrace) {
        this.exceptionClass = exceptionClass;
        this.commonMessage = commonMessage;
        this.occurrences = occurrences;
        this.stackTrace = stackTrace;
    }
}
