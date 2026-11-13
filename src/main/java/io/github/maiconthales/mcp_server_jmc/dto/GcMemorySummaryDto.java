package io.github.maiconthales.mcp_server_jmc.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Resume pausas de GC, heap e classes alocadas. */
@Getter
@Setter
public class GcMemorySummaryDto {
    /** Quantidade de coletas de lixo identificadas. */
    private long gcCount;

    /** Tempo total, em milissegundos, gasto nas pausas de coleta. */
    private long totalPauseMs;

    /** Maior pausa de coleta, em milissegundos. */
    private long maxPauseMs;

    /** Tamanho do heap, em bytes, antes da coleta. */
    private long heapBeforeBytes;

    /** Tamanho do heap, em bytes, depois da coleta. */
    private long heapAfterBytes;

    /** Classes que mais contribuíram para as alocações de memória. */
    private List<AllocatedClassDto> topAllocations;

    /**
     * Cria um resumo de memória e coleta.
     * @param gcCount quantidade de coletas
     * @param totalPauseMs pausa total
     * @param maxPauseMs maior pausa
     * @param heapBeforeBytes heap antes da coleta
     * @param heapAfterBytes heap depois da coleta
     * @param topAllocations maiores alocações
     */
    public GcMemorySummaryDto(long gcCount, long totalPauseMs, long maxPauseMs, long heapBeforeBytes,
            long heapAfterBytes, List<AllocatedClassDto> topAllocations) {
        this.gcCount = gcCount;
        this.totalPauseMs = totalPauseMs;
        this.maxPauseMs = maxPauseMs;
        this.heapBeforeBytes = heapBeforeBytes;
        this.heapAfterBytes = heapAfterBytes;
        this.topAllocations = topAllocations;
    }
}
