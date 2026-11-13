package io.github.maiconthales.mcp_server_jmc.validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Valida identificadores de arquivos e processos controlados pelo usuário. */
public final class PathValidator {
    /** Expressão usada para rejeitar segmentos de travessia de diretórios. */
    private static final Pattern PATH_TRAVERSAL = Pattern.compile("(^|/)\\.\\.(?:/|$)");

    /** Impede a instanciação da classe utilitária. */
    private PathValidator() { }

    /**
     * Valida a extensão, a ausência de travessia de diretórios e a legibilidade de um arquivo JFR.
     *
     * @param value caminho do arquivo informado pelo usuário
     * @return caminho absoluto e normalizado do arquivo JFR
     * @throws IllegalArgumentException se o caminho for nulo, vazio, contiver travessia de diretórios
     *                                  ou não possuir a extensão {@code .jfr}
     * @throws IOException se o arquivo não existir, não for regular ou não puder ser lido
     */
    public static Path validateJfr(String value) throws IOException {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("filePath is required");
        if (PATH_TRAVERSAL.matcher(value.replace('\\', '/')).find())
            throw new IllegalArgumentException("Path traversal is not allowed");
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!path.getFileName().toString().toLowerCase().endsWith(".jfr"))
            throw new IllegalArgumentException("Only .jfr files are accepted");
        if (!Files.isRegularFile(path) || !Files.isReadable(path))
            throw new IOException("JFR file does not exist or is not readable: " + path);
        return path;
    }

    /**
     * Valida um PID antes de qualquer chamada ao sistema operacional.
     *
     * @param pid identificador numérico do processo
     * @return o PID validado
     * @throws IllegalArgumentException se o PID for menor ou igual a zero ou exceder o valor máximo
     *                                  permitido para um inteiro
     */
    public static long validatePid(int pid) {
        if (pid <= 0 || pid > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid PID");
        return pid;
    }

    /**
     * Valida o nome de gravação aceito pelo comando {@code jcmd}.
     *
     * @param name nome da gravação informado pelo usuário
     * @return o nome de gravação validado
     * @throws IllegalArgumentException se o nome for nulo, estiver fora do limite de 1 a 80 caracteres
     *                                  ou contiver caracteres não permitidos
     */
    public static String validateRecordingName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9._-]{1,80}"))
            throw new IllegalArgumentException("Invalid recording name");
        return name;
    }
}

