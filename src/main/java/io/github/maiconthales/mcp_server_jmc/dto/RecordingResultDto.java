package io.github.maiconthales.mcp_server_jmc.dto;

import lombok.Getter;
import lombok.Setter;

/** Representa o resultado da criação de uma gravação JFR. */
@Getter
@Setter
public class RecordingResultDto {
    /** Identificador numérico do processo alvo. */
    private long pid;

    /** Identificador atribuído à gravação. */
    private String recordingId;

    /** Caminho esperado do arquivo da gravação. */
    private String path;

    /** Estado atual da gravação. */
    private String status;

    /** Cria um resultado de gravação.
     * @param pid identificador do processo
     * @param recordingId identificador da gravação
     * @param path caminho esperado do arquivo
     * @param status estado da gravação
     */
    public RecordingResultDto(long pid, String recordingId, String path, String status) {
        this.pid = pid;
        this.recordingId = recordingId;
        this.path = path;
        this.status = status;
    }
}
