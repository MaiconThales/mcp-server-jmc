package com.mcp.jmc.util;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class PathValidatorTest {
    @Test void acceptsReadableJfr() throws Exception {
        var file=Files.createTempFile("sample-", ".jfr");
        try { assertEquals(file.toAbsolutePath().normalize(),PathValidator.validateJfr(file.toString())); }
        finally { Files.deleteIfExists(file); }
    }
    @Test void rejectsUnsafeInputs() {
        assertThrows(IllegalArgumentException.class,()->PathValidator.validateJfr("../secret.txt"));
        assertThrows(IllegalArgumentException.class,()->PathValidator.validatePid(0));
        assertThrows(IllegalArgumentException.class,()->PathValidator.validateRecordingName("x; rm -rf /"));
    }
}
