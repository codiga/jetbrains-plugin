package io.codiga.plugins.jetbrains.model.rosie;

import io.codiga.plugins.jetbrains.utils.RosieLanguageSupport;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Rosie request object sent to the Codiga API.
 */
@AllArgsConstructor
public class RosieRequest {
    public String filename;
    /**
     * The Rosie language string.
     * <p>
     * @see RosieLanguageSupport
     */
    public String language;
    public String fileEncoding;
    /**
     * The base64-encoded version of the code to be analysed.
     */
    public String codeBase64;
    public List<RosieRule> rules;
    public boolean logOutput;
}
