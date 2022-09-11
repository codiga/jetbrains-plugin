package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

public class RosieRequest {
    public String filename;

    public String language;
    public String fileEncoding;
    public String codeBase64;
    public List<RosieRule> rules;
    public boolean logOutput;


    public RosieRequest(String filename, String language, String fileEncoding, String codeBase64, List<RosieRule> rosieRules, boolean logOutput) {
        this.filename = filename;
        this.language = language;
        this.fileEncoding = fileEncoding;
        this.codeBase64 = codeBase64;
        this.rules = rosieRules;
        this.logOutput = logOutput;
    }
}
