package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

public class RosieViolationFix {
    public String description;
    public List<RosieViolationFixEdit> edits;


    public RosieViolationFix(String description, List<RosieViolationFixEdit> edits) {
        this.description = description;
        this.edits = edits;
    }
}
