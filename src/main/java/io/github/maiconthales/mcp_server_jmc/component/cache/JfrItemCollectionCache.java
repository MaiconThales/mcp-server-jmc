package io.github.maiconthales.mcp_server_jmc.component.cache;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/** Mantém uma cache LRU limitada de eventos JFR por versão do arquivo.
 *
 * <p>A chave inclui o caminho normalizado e os metadados que identificam a
 * versão do arquivo. Assim, uma gravação alterada não reutiliza eventos
 * antigos e a capacidade máxima permanece limitada.</p>
 */
@Component
@RequiredArgsConstructor
public class JfrItemCollectionCache {
    private static final int CAPACITY = 4;
    private final Map<CacheKey, List<RecordedEvent>> entries = new LinkedHashMap<>(8, .75f, true);

    /** Obtém os eventos armazenados para a versão informada do arquivo.
     * @param path caminho do arquivo
     * @param size tamanho do arquivo
     * @param modified data de modificação
     * @return eventos armazenados ou nulo
     */
    public synchronized List<RecordedEvent> get(Path path, long size, FileTime modified) {
        return entries.get(new CacheKey(normalize(path), size, modified));
    }

    /** Armazena uma coleção imutável de eventos para a versão informada.
     * @param path caminho do arquivo
     * @param size tamanho do arquivo
     * @param modified data de modificação
     * @param events eventos a armazenar
     */
    public synchronized void put(Path path, long size, FileTime modified, List<RecordedEvent> events) {
        entries.put(new CacheKey(normalize(path), size, modified), List.copyOf(events));
        while (entries.size() > CAPACITY) {
            entries.remove(entries.keySet().iterator().next());
        }
    }

    /** Normaliza o caminho para que aliases do mesmo arquivo compartilhem a entrada.
     *
     * @param path caminho do arquivo
     * @return caminho absoluto e normalizado
     */
    private Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    /** Chave interna baseada no caminho e nos metadados do arquivo. */
    private static final class CacheKey {
        private final Path path;
        private final long size;
        private final FileTime modified;

        /** Cria uma chave da cache.
         * @param path caminho do arquivo
         * @param size tamanho do arquivo
         * @param modified data de modificação
         */
        private CacheKey(Path path, long size, FileTime modified) {
            this.path = path;
            this.size = size;
            this.modified = modified;
        }

        /** Compara chaves da cache.
         * @param other objeto comparado
         * @return verdadeiro quando as chaves são iguais
         */
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CacheKey key)) return false;
            return size == key.size && path.equals(key.path) && modified.equals(key.modified);
        }

        /** Calcula o hash da chave.
         * @return código hash da chave
         */
        @Override
        public int hashCode() {
            return java.util.Objects.hash(path, size, modified);
        }
    }
}
