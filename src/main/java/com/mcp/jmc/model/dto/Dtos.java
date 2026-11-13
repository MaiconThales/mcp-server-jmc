package com.mcp.jmc.model.dto;

import java.util.List;

public final class Dtos {
    private Dtos() { }
    public record ActiveJvm(long pid, String mainClass, String arguments) { }
    public record RecordingResult(long pid, String recordingId, String path, String status) { }
    public record RecordingStatus(long pid, String recordingName, String status, String path, String message) { }
    public record RuleDiagnosticDTO(String ruleName, double score, String severity, String summary, String solution) { }
    /** Top stack frame, including the declaring type and JVM descriptor. */
    public record CpuHotMethod(String method, long samples, double cpuPercent) { }
    public record GcMemorySummary(long gcCount, long totalPauseMs, long maxPauseMs,
                                  long heapBeforeBytes, long heapAfterBytes, List<Allocation> topAllocations) { }
    public record Allocation(String className, long bytes, long count) { }
    public record LockContention(String lockClass, long totalBlockedMs, long occurrences, String stackTrace) { }
    public record FrequentException(String exceptionClass, String commonMessage, long occurrences, String stackTrace) { }
}
