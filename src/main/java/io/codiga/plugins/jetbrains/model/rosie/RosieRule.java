package io.codiga.plugins.jetbrains.model.rosie;

public class RosieRule {
    public String id;
    public String contentBase64;
    public String language;
    public String type;
    public String entityChecked;
    public String pattern;

    public RosieRule(String id, String contentBase64, String language, String type, String entityChecked, String pattern) {
        this.id = id;
        this.contentBase64 = contentBase64;
        this.language = language;
        this.type = type;
        this.entityChecked = entityChecked;
        this.pattern = pattern;
    }
}
