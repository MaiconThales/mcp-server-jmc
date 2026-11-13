package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Resume alocações de uma classe Java. */
@Getter
@Setter
public class AllocatedClassDto {
    /** Nome totalmente qualificado da classe que realizou as alocações. */
    private String className;

    /** Quantidade de bytes alocados pela classe. */
    private long bytes;

    /** Quantidade de alocações realizadas pela classe. */
    private long count;

    /**
     * Cria um resumo de alocação.
     * @param className nome da classe
     * @param bytes bytes alocados
     * @param count quantidade de alocações
     */
    public AllocatedClassDto(String className, long bytes, long count) {
        this.className = className;
        this.bytes = bytes;
        this.count = count;
    }
}
