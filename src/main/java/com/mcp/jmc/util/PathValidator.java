package com.mcp.jmc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Validates all user-controlled file and process identifiers. */
public final class PathValidator {
    private PathValidator() { }

    public static Path validateJfr(String value) throws IOException {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("filePath is required");
        if (Pattern.compile("(^|/)\\.\\.(?:/|$)").matcher(value.replace('\\', '/')).find())
            throw new IllegalArgumentException("Path traversal is not allowed");
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!path.getFileName().toString().toLowerCase().endsWith(".jfr"))
            throw new IllegalArgumentException("Only .jfr files are accepted");
        if (!Files.isRegularFile(path) || !Files.isReadable(path))
            throw new IOException("JFR file does not exist or is not readable: " + path);
        return path;
    }

    public static long validatePid(int pid) {
        if (pid <= 0 || pid > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid PID");
        return pid;
    }

    public static String validateRecordingName(String name) {
        if (name == null || !name.matches("[A-Za-z0-9._-]{1,80}"))
            throw new IllegalArgumentException("Invalid recording name");
        return name;
    }
}
