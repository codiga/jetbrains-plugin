package io.codiga.plugins.jetbrains.model.rosie;

public class RosieViolationFixEdit {
    public RosiePosition start;
    public RosiePosition end;
    public String content;
    public String editType;


    public RosieViolationFixEdit(RosiePosition start, RosiePosition end, String editType, String content) {
        this.start = start;
        this.end = end;
        this.content = content;
        this.editType = editType;
    }
}
