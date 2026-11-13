package io.github.maiconthales.mcp_server_jmc.component.validator;

import io.github.maiconthales.mcp_server_jmc.validator.PathValidator;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica as validações de caminhos, PID e nomes de gravação. */
class PathValidatorTest {
    /** Confirma que um arquivo JFR legível é aceito. */
    @Test
    void acceptsReadableJfr() throws Exception {
        var file=Files.createTempFile("sample-", ".jfr");
        try { assertEquals(file.toAbsolutePath().normalize(), PathValidator.validateJfr(file.toString())); }
        finally { Files.deleteIfExists(file); }
    }
    /** Confirma que entradas inseguras são rejeitadas. */
    @Test
    void rejectsUnsafeInputs() {
        assertThrows(IllegalArgumentException.class,()->PathValidator.validateJfr("../secret.txt"));
        assertThrows(IllegalArgumentException.class,()->PathValidator.validatePid(0));
        assertThrows(IllegalArgumentException.class,()->PathValidator.validateRecordingName("x; rm -rf /"));
    }
}
