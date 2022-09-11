package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

public class RosieViolation {
    public String message;
    public RosiePosition start;
    public RosiePosition end;
    public String severity;
    public String category;
    public List<RosieViolationFix> fixes;


    public RosieViolation(RosiePosition start, RosiePosition end, String message, String severity, String category, List<RosieViolationFix> fixes) {
        this.message = message;
        this.start = start;
        this.end = end;
        this.severity = severity;
        this.category = category;
        this.fixes = fixes;
    }
}

