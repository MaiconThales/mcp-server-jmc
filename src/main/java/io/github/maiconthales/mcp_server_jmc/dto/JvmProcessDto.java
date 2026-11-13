package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Representa uma JVM anexável ao processo atual. */
@Getter
@Setter
public class JvmProcessDto {
    /** Identificador numérico do processo da JVM. */
    private long pid;

    /** Nome da classe principal executada pela JVM. */
    private String mainClass;

    /** Argumentos informados ao processo da JVM. */
    private String arguments;

    /**
     * Cria uma representação de processo JVM.
     *
     * @param pid identificador do processo
     * @param mainClass classe principal
     * @param arguments argumentos do processo
     */
    public JvmProcessDto(long pid, String mainClass, String arguments) {
        this.pid = pid;
        this.mainClass = mainClass;
        this.arguments = arguments;
    }
}
