package com.mcp.jmc.tools;

import com.mcp.jmc.model.dto.Dtos;
import com.mcp.jmc.service.JcmdExecutorService;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProcessControlTools {
    private final JcmdExecutorService service;
    public ProcessControlTools(JcmdExecutorService service) { this.service=service; }
    public List<Dtos.ActiveJvm> list_active_jvms() { return service.listJvms(); }
    public Dtos.RecordingResult start_jfr_recording(int pid) throws Exception { return start_jfr_recording(pid,30,"profile"); }
    public Dtos.RecordingResult start_jfr_recording(int pid, int durationSeconds, String profile) throws Exception { return service.startRecording(pid,durationSeconds,profile); }
    public Dtos.RecordingStatus check_jfr_recording(int pid, String recordingName) throws Exception { return service.checkRecording(pid,recordingName); }
    public Dtos.RecordingStatus check_jfr_recording(int pid, String recordingName, boolean forceDump) throws Exception { return service.checkRecording(pid,recordingName,forceDump); }
}
