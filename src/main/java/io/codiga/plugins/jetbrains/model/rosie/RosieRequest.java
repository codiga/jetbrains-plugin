package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

/**
 * Rosie request object sent to the Codiga API.
 */
public class RosieRequest {
    public String filename;
    /**
     * The Rosie language string.
     * <p>
     * See {@link io.codiga.plugins.jetbrains.utils.RosieUtils#getRosieLanguage(io.codiga.api.type.LanguageEnumeration)}
     * and {@link io.codiga.plugins.jetbrains.services.RosieImpl#SUPPORTED_LANGUAGES}.
     */
    public String language;
    public String fileEncoding;
    /**
     * The base64-encoded version of the code to be analysed.
     */
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
