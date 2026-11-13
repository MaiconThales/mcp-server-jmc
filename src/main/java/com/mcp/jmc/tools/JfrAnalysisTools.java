package com.mcp.jmc.tools;

import com.mcp.jmc.model.dto.Dtos;
import com.mcp.jmc.service.JfrAggregatorService;
import com.mcp.jmc.service.JfrRuleEngineService;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JfrAnalysisTools {
    private final JfrAggregatorService aggregator; private final JfrRuleEngineService rules;
    public JfrAnalysisTools(JfrAggregatorService aggregator,JfrRuleEngineService rules){this.aggregator=aggregator;this.rules=rules;}
    public List<Dtos.RuleDiagnosticDTO> analyze_jfr_automated_rules(String filePath,double minScore)throws Exception{return rules.analyze(filePath,minScore);}
    public List<Dtos.RuleDiagnosticDTO> analyze_jfr_automated_rules(String filePath)throws Exception{return analyze_jfr_automated_rules(filePath,50.0);}
    public List<Dtos.CpuHotMethod> get_cpu_hot_methods(String filePath,int topN)throws Exception{return aggregator.cpu(filePath,topN);}
    public List<Dtos.CpuHotMethod> get_cpu_hot_methods(String filePath)throws Exception{return get_cpu_hot_methods(filePath,10);}
    public Dtos.GcMemorySummary analyze_gc_and_memory(String filePath,int topClasses)throws Exception{return aggregator.gc(filePath,topClasses);}
    public Dtos.GcMemorySummary analyze_gc_and_memory(String filePath)throws Exception{return analyze_gc_and_memory(filePath,10);}
    public List<Dtos.LockContention> find_lock_contentions(String filePath,long minDurationMs)throws Exception{return aggregator.locks(filePath,minDurationMs);}
    public List<Dtos.LockContention> find_lock_contentions(String filePath)throws Exception{return find_lock_contentions(filePath,50);}
    public List<Dtos.FrequentException> get_frequent_exceptions(String filePath,int topN)throws Exception{return aggregator.exceptions(filePath,topN);}
    public List<Dtos.FrequentException> get_frequent_exceptions(String filePath)throws Exception{return get_frequent_exceptions(filePath,10);}
}
