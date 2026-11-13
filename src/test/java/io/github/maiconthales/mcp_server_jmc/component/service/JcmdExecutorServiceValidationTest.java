package io.github.maiconthales.mcp_server_jmc.component.service;

import io.github.maiconthales.mcp_server_jmc.service.JcmdExecutorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifica a validação de argumentos antes da execução do jcmd. */
class JcmdExecutorServiceValidationTest {
    private final JcmdExecutorService service = new JcmdExecutorService();

    /** Confirma que argumentos inválidos são rejeitados antecipadamente. */
    @Test
    void rejectsInvalidRecordingArgumentsBeforeSpawningJcmd() {
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(0, 30, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 0, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 301, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 30, "unsafe"));
        assertThrows(IllegalArgumentException.class, () -> service.checkRecording(1, "bad name"));
    }
}
