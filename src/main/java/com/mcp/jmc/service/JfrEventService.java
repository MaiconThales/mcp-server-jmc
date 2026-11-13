package com.mcp.jmc.service;

import com.mcp.jmc.util.PathValidator;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JfrEventService {
    private record Key(Path path, long size, FileTime modified) { }
    private final Map<Key, List<RecordedEvent>> cache = new LinkedHashMap<>(8, .75f, true) {
        protected boolean removeEldestEntry(Map.Entry<Key, List<RecordedEvent>> e) { return size() > 4; }
    };
    public synchronized List<RecordedEvent> events(String file) throws IOException {
        Path path = PathValidator.validateJfr(file);
        Key key = new Key(path, java.nio.file.Files.size(path), java.nio.file.Files.getLastModifiedTime(path));
        var found = cache.get(key); if (found != null) return found;
        List<RecordedEvent> result = new ArrayList<>();
        try (var recording = new RecordingFile(path)) { while (recording.hasMoreEvents()) result.add(recording.readEvent()); }
        var immutable = List.copyOf(result); cache.put(key, immutable); return immutable;
    }
}
