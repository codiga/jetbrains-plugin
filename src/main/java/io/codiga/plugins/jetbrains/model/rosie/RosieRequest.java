package io.codiga.plugins.jetbrains.model.rosie;

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
     * See {@link io.codiga.plugins.jetbrains.utils.RosieUtils#getRosieLanguage(io.codiga.api.type.LanguageEnumeration)}
     * and {@link io.codiga.plugins.jetbrains.rosie.RosieImpl#SUPPORTED_LANGUAGES}.
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
