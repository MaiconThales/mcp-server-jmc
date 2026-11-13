package com.mcp.jmc.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JcmdExecutorServiceValidationTest {
    private final JcmdExecutorService service = new JcmdExecutorService();

    @Test
    void rejectsInvalidRecordingArgumentsBeforeSpawningJcmd() {
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(0, 30, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 0, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 301, "profile"));
        assertThrows(IllegalArgumentException.class, () -> service.startRecording(1, 30, "unsafe"));
        assertThrows(IllegalArgumentException.class, () -> service.checkRecording(1, "bad name"));
    }
}
