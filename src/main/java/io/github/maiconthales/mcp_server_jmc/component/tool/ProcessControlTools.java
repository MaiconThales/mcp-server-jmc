package io.github.maiconthales.mcp_server_jmc.component.tool;

import io.github.maiconthales.mcp_server_jmc.service.JcmdExecutorService;
import io.github.maiconthales.mcp_server_jmc.dto.JfrRecordingStatusDto;
import io.github.maiconthales.mcp_server_jmc.dto.JvmProcessDto;
import io.github.maiconthales.mcp_server_jmc.dto.RecordingResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Expõe operações de controle de processos e gravações JFR ao MCP.
 *
 * <p>Os métodos Java seguem a convenção {@code lowerCamelCase}. Os nomes
 * externos das ferramentas MCP são definidos separadamente no registro do
 * servidor e preservam o contrato com sublinhados.</p>
 */
@Component
@RequiredArgsConstructor
public class ProcessControlTools {

    /** Serviço responsável por executar operações na JVM alvo. */
    private final JcmdExecutorService service;

    /**
     * Lista as JVMs anexáveis.
     *
     * @return processos JVM disponíveis
     */
    public List<JvmProcessDto> listActiveJvms() {
        return service.listJvms();
    }

    /**
     * Inicia uma gravação com os valores padrão.
     *
     * @param pid identificador do processo alvo
     * @return resultado da gravação
     * @throws Exception se a gravação não puder ser iniciada
     */
    public RecordingResultDto startJfrRecording(int pid) throws Exception {
        return startJfrRecording(pid, 30, "profile");
    }

    /**
     * Inicia uma gravação JFR com duração e perfil informados.
     *
     * @param pid identificador do processo alvo
     * @param durationSeconds duração em segundos
     * @param profile perfil JFR
     * @return resultado da gravação
     * @throws Exception se a gravação não puder ser iniciada
     */
    public RecordingResultDto startJfrRecording(int pid, int durationSeconds, String profile)
            throws Exception {
        return service.startRecording(pid, durationSeconds, profile);
    }

    /**
     * Consulta uma gravação e realiza o dump padrão.
     *
     * @param pid identificador do processo alvo
     * @param recordingName nome da gravação
     * @return status da gravação
     * @throws Exception se a consulta falhar
     */
    public JfrRecordingStatusDto checkJfrRecording(int pid, String recordingName) throws Exception {
        return service.checkRecording(pid, recordingName);
    }

    /**
     * Consulta uma gravação com controle explícito de dump.
     *
     * @param pid identificador do processo alvo
     * @param recordingName nome da gravação
     * @param forceDump indica se o dump deve ser forçado
     * @return status da gravação
     * @throws Exception se a consulta falhar
     */
    public JfrRecordingStatusDto checkJfrRecording(int pid, String recordingName, boolean forceDump)
            throws Exception {
        return service.checkRecording(pid, recordingName, forceDump);
    }
}
