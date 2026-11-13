package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Representa o resultado de uma operação sobre uma gravação JFR. */
@Getter
@Setter
public class JfrRecordingStatusDto {
    /** Identificador numérico do processo que mantém a gravação. */
    private long pid;

    /** Nome da gravação JFR consultada ou iniciada. */
    private String recordingName;

    /** Estado atual da gravação JFR. */
    private String status;

    /** Caminho do arquivo gerado, quando disponível. */
    private String path;

    /** Mensagem complementar sobre a operação realizada. */
    private String message;

    /**
     * Cria o status de uma gravação JFR.
     *
     * @param pid identificador do processo
     * @param recordingName nome da gravação
     * @param status estado da gravação
     * @param path caminho do arquivo, quando disponível
     * @param message mensagem da operação
     */
    public JfrRecordingStatusDto(long pid, String recordingName, String status, String path, String message) {
        this.pid = pid;
        this.recordingName = recordingName;
        this.status = status;
        this.path = path;
        this.message = message;
    }
}
