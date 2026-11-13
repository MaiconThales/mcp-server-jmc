package com.mcp.jmc.service;

import com.mcp.jmc.model.dto.Dtos;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordedClass;
import jdk.jfr.consumer.RecordedMethod;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JfrAggregatorService {
    private static final int MAX_LOCK_GROUPS = 50;
    private final JfrEventService source;
    public JfrAggregatorService(JfrEventService source) { this.source = source; }
    public List<Dtos.CpuHotMethod> cpu(String file, int topN) throws Exception {
        int n = bounded(topN); var counts = new HashMap<String, Long>();
        for (var e : source.events(file)) if (type(e, "jdk.ExecutionSample")) {
            String method = topFrame(e); counts.merge(method, 1L, Long::sum);
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(n)
                .map(x -> new Dtos.CpuHotMethod(x.getKey(), x.getValue(), total == 0 ? 0 : x.getValue() * 100.0 / total)).toList();
    }
    public Dtos.GcMemorySummary gc(String file, int topClasses) throws Exception {
        long count=0,total=0,max=0,before=0,after=0; var allocations=new HashMap<String,long[]>();
        for (var e : source.events(file)) {
            if (type(e,"jdk.GarbageCollection")) { long ms=durationMs(e); count++; total+=ms; max=Math.max(max,ms); }
            if (type(e,"jdk.GCHeapSummary")) {
                var when = text(e, "when");
                var heap = value(e,"heapUsed");
                if (when != null && when.toLowerCase().contains("before")) before = heap;
                if (when != null && when.toLowerCase().contains("after")) after = heap;
            }
            if (type(e,"jdk.ObjectAllocationInNewTLAB")) { String c=className(e); long[] a=allocations.computeIfAbsent(c,k->new long[2]); a[0]+=value(e,"allocationSize"); a[1]++; }
        }
        var top=allocations.entrySet().stream().sorted((a,b)->Long.compare(b.getValue()[0],a.getValue()[0])).limit(bounded(topClasses))
                .map(x->new Dtos.Allocation(x.getKey(),x.getValue()[0],x.getValue()[1])).toList();
        return new Dtos.GcMemorySummary(count,total,max,before,after,top);
    }
    public List<Dtos.LockContention> locks(String file, long minMs) throws Exception {
        var grouped=new HashMap<String,long[]>(); var traces=new HashMap<String,String>();
        for(var e:source.events(file)) if(type(e,"jdk.JavaMonitorEnter") && durationMs(e)>=Math.max(0,minMs)) { String c=className(e); var a=grouped.computeIfAbsent(c,k->new long[2]); a[0]+=durationMs(e); a[1]++; traces.putIfAbsent(c, stack(e)); }
        return grouped.entrySet().stream().sorted((a,b)->Long.compare(b.getValue()[0],a.getValue()[0])).limit(MAX_LOCK_GROUPS).map(x->new Dtos.LockContention(x.getKey(),x.getValue()[0],x.getValue()[1],traces.get(x.getKey()))).toList();
    }
    public List<Dtos.FrequentException> exceptions(String file, int topN) throws Exception {
        record Key(String type,String msg) {} var groups=new HashMap<String,long[]>(); var msg=new HashMap<String,String>(); var trace=new HashMap<String,String>();
        for(var e:source.events(file)) if(type(e,"jdk.JavaExceptionThrow")) { String c=className(e); var a=groups.computeIfAbsent(c,k->new long[1]); a[0]++; String m=text(e,"message"); if(m!=null) msg.putIfAbsent(c,m); trace.putIfAbsent(c,stack(e)); }
        return groups.entrySet().stream().sorted((a,b)->Long.compare(b.getValue()[0],a.getValue()[0])).limit(bounded(topN)).map(x->new Dtos.FrequentException(x.getKey(),msg.getOrDefault(x.getKey(),""),x.getValue()[0],trace.get(x.getKey()))).toList();
    }
    private static int bounded(int n){return Math.max(1,Math.min(n,100));}
    private static boolean type(RecordedEvent e,String t){return e.getEventType().getName().equals(t);}
    private static long durationMs(RecordedEvent e){return Math.max(0,Duration.between(e.getStartTime(),e.getEndTime()).toMillis());}
    private static long value(RecordedEvent e,String f){try{return e.getLong(f);}catch(RuntimeException x){return 0;}}
    private static String text(RecordedEvent e,String f){try{return e.getString(f);}catch(RuntimeException x){return null;}}
    private static String className(RecordedEvent e){
        for(String f:List.of("throwable","monitorClass","eventClass","objectClass")) try {
            var o=e.getValue(f);
            if (o instanceof RecordedClass c) return c.getName();
            if (o instanceof RecordedMethod m) return m.getType().getName();
            if(o!=null) { var s = o.toString(); if (!s.isBlank()) return s; }
        } catch(RuntimeException ignored) { }
        return "unknown";
    }
    private static String topFrame(RecordedEvent e) {
        var s=e.getStackTrace();
        if (s == null || s.getFrames().isEmpty()) return "unknown";
        var frame=s.getFrames().getFirst();
        var method=frame.getMethod();
        return method.getType().getName()+"."+method.getName()+method.getDescriptor();
    }
    private static String stack(RecordedEvent e){RecordedStackTrace s=e.getStackTrace(); return s==null?"":s.getFrames().stream().limit(5).map(Object::toString).collect(Collectors.joining("\n"));}
}
