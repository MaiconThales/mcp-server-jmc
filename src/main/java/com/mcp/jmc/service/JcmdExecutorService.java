package com.mcp.jmc.service;

import com.mcp.jmc.model.dto.Dtos;
import com.mcp.jmc.util.PathValidator;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JcmdExecutorService {
    public List<Dtos.ActiveJvm> listJvms() {
        return VirtualMachine.list().stream().map(d -> {
            var display = d.displayName() == null ? "" : d.displayName().trim();
            var split = display.indexOf(' ');
            var main = split < 0 ? display : display.substring(0, split);
            var args = split < 0 ? "" : display.substring(split + 1).trim();
            return new Dtos.ActiveJvm(parsePid(d), main, args);
        }).toList();
    }

    public Dtos.RecordingResult startRecording(int pid, int durationSeconds, String template) throws IOException, InterruptedException {
        PathValidator.validatePid(pid);
        if (durationSeconds < 1 || durationSeconds > 300) throw new IllegalArgumentException("durationSeconds must be 1..300");
        if (!"default".equals(template) && !"profile".equals(template)) throw new IllegalArgumentException("template must be default or profile");
        String id = "jmc-" + pid + "-" + Instant.now().toEpochMilli();
        Path target = Path.of(System.getProperty("java.io.tmpdir"), id + ".jfr");
        run(pid, "JFR.start", "name=" + id, "settings=" + template, "duration=" + durationSeconds + "s", "filename=" + target);
        return new Dtos.RecordingResult(pid, id, target.toString(), "RUNNING");
    }

    public Dtos.RecordingStatus checkRecording(int pid, String recordingName) throws IOException, InterruptedException {
        return checkRecording(pid, recordingName, true);
    }

    /** Checks a recording and, by default, stops it so a complete JFR is immediately available. */
    public Dtos.RecordingStatus checkRecording(int pid, String recordingName, boolean forceDump)
            throws IOException, InterruptedException {
        PathValidator.validatePid(pid); PathValidator.validateRecordingName(recordingName);
        String output = run(pid, "JFR.check", "name=" + recordingName);
        Path path = Path.of(System.getProperty("java.io.tmpdir"), recordingName + ".jfr");
        boolean running = output.toLowerCase().matches("(?s).*\\brunning\\b.*");
        if (forceDump && running) {
            output += "\n" + run(pid, "JFR.stop", "name=" + recordingName, "filename=" + path);
            running = false;
        } else if (forceDump && !Files.isRegularFile(path)) {
            output += "\n" + run(pid, "JFR.dump", "name=" + recordingName, "filename=" + path);
        }
        return new Dtos.RecordingStatus(pid, recordingName, running ? "RUNNING" : "STOPPED",
                Files.isRegularFile(path) ? path.toString() : null, output.trim());
    }

    private String run(int pid, String... args) throws IOException, InterruptedException {
        var command = new ArrayList<String>(); command.add("jcmd"); command.add(Long.toString(pid)); command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IOException("jcmd timed out"); }
        String output = new String(process.getInputStream().readAllBytes());
        if (process.exitValue() != 0) throw new IOException("jcmd failed: " + output);
        return output;
    }
    private static long parsePid(VirtualMachineDescriptor d) { try { return Long.parseLong(d.id()); } catch (NumberFormatException e) { return -1; } }
}
