package io.github.maiconthales.mcp_server_jmc.service;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.github.maiconthales.mcp_server_jmc.validator.PathValidator;
import io.github.maiconthales.mcp_server_jmc.dto.JfrRecordingStatusDto;
import io.github.maiconthales.mcp_server_jmc.dto.JvmProcessDto;
import io.github.maiconthales.mcp_server_jmc.dto.RecordingResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Executa operações de diagnóstico em JVMs por meio da Attach API e do jcmd. */
@Service
@RequiredArgsConstructor
public class JcmdExecutorService {

    /** Lista os processos JVM visíveis por meio da Attach API.
     *
     * @return processos JVM encontrados
     */
    public List<JvmProcessDto> listJvms() {
        return VirtualMachine.list().stream()
                .map(descriptor -> new JvmProcessDto(
                        parsePid(descriptor),
                        mainClass(descriptor),
                        applicationArguments(descriptor)))
                .toList();
    }

    /** Inicia uma gravação JFR depois de validar todos os parâmetros externos.
     *
     * @param pid identificador do processo alvo
     * @param durationSeconds duração da gravação em segundos
     * @param template modelo JFR a utilizar
     * @return resultado da gravação iniciada
     * @throws IOException se a execução do comando falhar
     * @throws InterruptedException se a thread for interrompida
     */
    public RecordingResultDto startRecording(int pid, int durationSeconds, String template)
            throws IOException, InterruptedException {
        PathValidator.validatePid(pid);
        validateDuration(durationSeconds);
        validateTemplate(template);

        String recordingId = "jmc-" + pid + "-" + Instant.now().toEpochMilli();
        Path target = Path.of(System.getProperty("java.io.tmpdir"), recordingId + ".jfr");
        run(pid, "JFR.start", "name=" + recordingId, "settings=" + template,
                "duration=" + durationSeconds + "s", "filename=" + target);
        return new RecordingResultDto(pid, recordingId, target.toString(), "RUNNING");
    }

    /** Consulta uma gravação e solicita seu dump completo.
     *
     * @param pid identificador do processo alvo
     * @param recordingName nome da gravação
     * @return status da gravação
     * @throws IOException se a execução do comando falhar
     * @throws InterruptedException se a thread for interrompida
     */
    public JfrRecordingStatusDto checkRecording(int pid, String recordingName)
            throws IOException, InterruptedException {
        return checkRecording(pid, recordingName, true);
    }

    /** Consulta uma gravação e opcionalmente a encerra para disponibilizar o arquivo.
     *
     * @param pid identificador do processo alvo
     * @param recordingName nome da gravação
     * @param forceDump indica se o arquivo deve ser disponibilizado
     * @return status da gravação
     * @throws IOException se a execução do comando falhar
     * @throws InterruptedException se a thread for interrompida
     */
    public JfrRecordingStatusDto checkRecording(int pid, String recordingName, boolean forceDump)
            throws IOException, InterruptedException {
        PathValidator.validatePid(pid);
        PathValidator.validateRecordingName(recordingName);

        String output = run(pid, "JFR.check", "name=" + recordingName);
        Path recordingPath = Path.of(System.getProperty("java.io.tmpdir"), recordingName + ".jfr");
        boolean running = output.toLowerCase().matches("(?s).*\\brunning\\b.*");
        if (forceDump && running) {
            output += "\n" + run(pid, "JFR.stop", "name=" + recordingName,
                    "filename=" + recordingPath);
            running = false;
        } else if (forceDump && !Files.isRegularFile(recordingPath)) {
            output += "\n" + run(pid, "JFR.dump", "name=" + recordingName,
                    "filename=" + recordingPath);
        }

        return new JfrRecordingStatusDto(pid, recordingName, running ? "RUNNING" : "STOPPED",
                Files.isRegularFile(recordingPath) ? recordingPath.toString() : null,
                output.trim());
    }

    /** Executa jcmd com tempo máximo e captura sua saída.
     *
     * @param pid identificador validado do processo
     * @param args argumentos do comando
     * @return saída do comando
     * @throws IOException se o processo falhar ou exceder o tempo
     * @throws InterruptedException se a thread for interrompida
     */
    private String run(int pid, String... args) throws IOException, InterruptedException {
        var command = new ArrayList<String>();
        command.add("jcmd");
        command.add(Long.toString(pid));
        command.addAll(List.of(args));

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("jcmd timed out");
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.exitValue() != 0) {
            throw new IOException("jcmd failed: " + output);
        }
        return output;
    }

    /** Extrai o nome da classe principal do descritor da JVM.
     *
     * @param descriptor descritor retornado pela Attach API
     * @return nome da classe principal ou texto vazio
     */
    private static String mainClass(VirtualMachineDescriptor descriptor) {
        String displayName = displayName(descriptor);
        int separator = displayName.indexOf(' ');
        return separator < 0 ? displayName : displayName.substring(0, separator);
    }

    /** Extrai os argumentos da aplicação do descritor da JVM.
     *
     * @param descriptor descritor retornado pela Attach API
     * @return argumentos da aplicação ou texto vazio
     */
    private static String applicationArguments(VirtualMachineDescriptor descriptor) {
        String displayName = displayName(descriptor);
        int separator = displayName.indexOf(' ');
        return separator < 0 ? "" : displayName.substring(separator + 1).trim();
    }

    /** Normaliza o nome exibido pela Attach API.
     *
     * @param descriptor descritor retornado pela Attach API
     * @return nome exibido sem espaços excedentes
     */
    private static String displayName(VirtualMachineDescriptor descriptor) {
        return descriptor.displayName() == null ? "" : descriptor.displayName().trim();
    }

    /** Converte o identificador do Attach API em PID numérico.
     *
     * @param descriptor descritor da máquina virtual
     * @return PID numérico ou {@code -1} quando inválido
     */
    private static long parsePid(VirtualMachineDescriptor descriptor) {
        try {
            return Long.parseLong(descriptor.id());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    /** Valida a duração permitida para uma gravação.
     *
     * @param durationSeconds duração solicitada em segundos
     * @throws IllegalArgumentException se a duração estiver fora do intervalo permitido
     */
    private static void validateDuration(int durationSeconds) {
        if (durationSeconds < 1 || durationSeconds > 300) {
            throw new IllegalArgumentException("durationSeconds must be 1..300");
        }
    }

    /** Valida o modelo JFR aceito pelo serviço.
     *
     * @param template modelo informado pelo chamador
     * @throws IllegalArgumentException se o modelo não for {@code default} ou {@code profile}
     */
    private static void validateTemplate(String template) {
        if (!"default".equals(template) && !"profile".equals(template)) {
            throw new IllegalArgumentException("template must be default or profile");
        }
    }
}
