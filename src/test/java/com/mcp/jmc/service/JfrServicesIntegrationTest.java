package com.mcp.jmc.service;

import jdk.jfr.Recording;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the stable JDK RecordingFile path with a real, generated JFR. */
class JfrServicesIntegrationTest {
    @Test
    void loadsRealRecordingAndKeepsBoundedAggregates(@TempDir Path temp) throws Exception {
        Path jfr = createRecording(temp);
        var events = new JfrEventService();

        var first = events.events(jfr.toString());
        assertFalse(first.isEmpty(), "RecordingFile should read events from the generated recording");
        assertSame(first, events.events(jfr.toString()), "identical file should be served from the cache");

        var aggregate = new JfrAggregatorService(events);
        assertNotNull(aggregate.cpu(jfr.toString(), 0));
        assertTrue(aggregate.cpu(jfr.toString(), 1000).size() <= 100);
        assertNotNull(aggregate.gc(jfr.toString(), 0));
        assertNotNull(aggregate.locks(jfr.toString(), -1));
        assertNotNull(aggregate.exceptions(jfr.toString(), 0));
    }

    @Test
    void lockContentionsAreBounded(@TempDir Path temp) throws Exception {
        Path jfr = createRecording(temp);
        var aggregate = new JfrAggregatorService(new JfrEventService());

        assertTrue(aggregate.locks(jfr.toString(), -1).size() <= 50,
                "lock contention results must be capped for safe MCP responses");
    }

    @Test
    void ruleFacadeValidatesScoreAndReturnsOnlyMatchingDiagnostics(@TempDir Path temp) throws Exception {
        Path jfr = createRecording(temp);
        var source = new JfrEventService();
        var rules = new JfrRuleEngineService(new JfrAggregatorService(source));

        assertThrows(IllegalArgumentException.class, () -> rules.analyze(jfr.toString(), -0.01));
        assertThrows(IllegalArgumentException.class, () -> rules.analyze(jfr.toString(), 100.01));
        assertThrows(IllegalArgumentException.class, () -> rules.analyze("../sample.jfr", 50),
                "the rule tool must reject traversal before invoking JMC");
        assertTrue(rules.analyze(jfr.toString(), 100).stream().allMatch(r -> r.score() >= 100));
    }

    private static Path createRecording(Path directory) throws Exception {
        Path output = directory.resolve("sample.jfr");
        try (var recording = new Recording()) {
            recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(1));
            recording.enable("jdk.JavaExceptionThrow");
            recording.enable("jdk.ObjectAllocationInNewTLAB").withThreshold(Duration.ZERO);
            recording.enable("jdk.GarbageCollection");
            recording.enable("jdk.JavaMonitorEnter").withThreshold(Duration.ZERO);
            recording.start();
            long until = System.nanoTime() + Duration.ofMillis(350).toNanos();
            while (System.nanoTime() < until) {
                try { throw new IllegalStateException("sample"); }
                catch (IllegalStateException ignored) { }
                var allocation = new byte[256];
                if (allocation.length == 0) throw new AssertionError();
                busyWork();
            }
            recording.stop();
            recording.dump(output);
        }
        assertTrue(Files.size(output) > 0);
        return output;
    }

    private static void busyWork() {
        double value = 0;
        for (int i = 0; i < 10_000; i++) value += Math.sqrt(i);
        if (value == -1) throw new AssertionError();
    }
}
