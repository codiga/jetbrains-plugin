package io.codiga.plugins.jetbrains.rosie;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.utils.LanguageUtils;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import io.codiga.plugins.jetbrains.model.rosie.RosieRequest;
import io.codiga.plugins.jetbrains.model.rosie.RosieResponse;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.RosieLanguageSupport.getRosieLanguage;
import static io.codiga.plugins.jetbrains.utils.RosieLanguageSupport.isLanguageSupported;
import static io.codiga.plugins.jetbrains.utils.UserAgentUtils.getUserAgent;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of the Rosie API.
 */
public class RosieApiImpl implements RosieApi {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final String ROSIE_POST_URL = "https://analysis.codiga.io/analyze";
    private static final Gson GSON = new Gson();

    public RosieApiImpl() {
        // no constructor instructions
    }

    @Override
    @NotNull
    public List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project) {
        if (psiFile.getVirtualFile() == null) {
            return List.of();
        }

        LanguageEnumeration fileLanguage = LanguageUtils.getLanguageFromFilename(psiFile.getVirtualFile().getCanonicalPath());

        // not supported, we exit right away
        if (!isLanguageSupported(fileLanguage)) {
            LOGGER.info(String.format("language not supported %s", fileLanguage));
            return List.of();
        }

        try {
            byte[] fileText = ReadAction.compute(() -> psiFile.getText().getBytes());
            String codeBase64 = Base64.getEncoder().encodeToString(fileText);

            // Prepare the request
            var rosieRules = RosieRulesCache.getInstance(project).getRosieRules(fileLanguage, psiFile.getVirtualFile().getPath());
            //If there is no rule for the target language, then Rosie is not called, and no annotation is performed
            if (rosieRules.isEmpty()) {
                return List.of();
            }

            RosieRequest request = new RosieRequest(psiFile.getName(), getRosieLanguage(fileLanguage), "utf8", codeBase64, rosieRules, true);
            String requestString = GSON.toJson(request);
            StringEntity postingString = new StringEntity(requestString); //gson.toJson() converts your pojo to json
            HttpPost httpPost = new HttpPost(ROSIE_POST_URL);
            httpPost.addHeader("User-Agent", getUserAgent());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(postingString);

            CloseableHttpClient client = HttpClients.createDefault();
            long requestTimestamp = System.currentTimeMillis();
            HttpResponse response = client.execute(httpPost);
            LOGGER.debug("Rules sent in request " + requestTimestamp + ": " + rosieRules.stream().map(RosieRule::toString).collect(toList()));
            List<RosieAnnotation> annotations = List.of();
            if (response.getEntity() != null) {
                String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Response received from request " + requestTimestamp + ": " + result);
                RosieResponse rosieResponse = GSON.fromJson(result, RosieResponse.class);

                //If there is no error returned, collect the violations
                if (rosieResponse.errors == null || rosieResponse.errors.isEmpty()) {
                    annotations = rosieResponse.ruleResponses.stream()
                        .flatMap(res -> res.violations.stream()
                            //'distinct()' makes sure that if multiple, completely identical, violations are returned
                            // for the same problem from Rosie, only one instance is shown by RosieAnnotator.
                            .distinct()
                            .map(violation -> {
                                var rule = RosieRulesCache.getInstance(project).getRuleWithNamesFor(fileLanguage, res.identifier);
                                return new RosieAnnotation(rule.ruleName, rule.rulesetName, violation);
                            }))
                        .collect(toList());
                }
            }
            client.close();
            return annotations;
        } catch (UnknownHostException unknownHostException) {
            LOGGER.warn("[RosieApiImpl] Could not connect to analysis.codiga.io.", unknownHostException);
            return List.of();
        } catch (IOException unsupportedEncodingException) {
            LOGGER.warn("[RosieApiImpl] ClientProtocolException", unsupportedEncodingException);
            return List.of();
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.warn("[RosieApiImpl] cannot decode JSON", jsonSyntaxException);
            return List.of();
        }
    }
}
