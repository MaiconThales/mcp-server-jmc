package io.github.maiconthales.mcp_server_jmc.service;

import io.github.maiconthales.mcp_server_jmc.validator.PathValidator;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import io.github.maiconthales.mcp_server_jmc.component.cache.JfrItemCollectionCache;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Carrega eventos de arquivos JFR e reutiliza resultados por versão do arquivo. */
@Service
@RequiredArgsConstructor
public class JfrEventService {
    private final JfrItemCollectionCache cache;

    /** Lê e agrega todos os eventos de uma gravação JFR validada.
     * @param file caminho do arquivo JFR
     * @return eventos da gravação
     * @throws IOException se o arquivo não puder ser lido
     */
    public synchronized List<RecordedEvent> events(String file) throws IOException {
        Path path = PathValidator.validateJfr(file);
        var size = java.nio.file.Files.size(path);
        var modified = java.nio.file.Files.getLastModifiedTime(path);
        var found = cache.get(path, size, modified); if (found != null) return found;
        List<RecordedEvent> result = new ArrayList<>();
        try (var recording = new RecordingFile(path)) { while (recording.hasMoreEvents()) result.add(recording.readEvent()); }
        var immutable = List.copyOf(result); cache.put(path, size, modified, immutable); return immutable;
    }
}

