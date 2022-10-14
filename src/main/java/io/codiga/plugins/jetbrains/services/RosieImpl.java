package io.codiga.plugins.jetbrains.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import io.codiga.plugins.jetbrains.model.rosie.RosieRequest;
import io.codiga.plugins.jetbrains.model.rosie.RosieResponse;
import io.codiga.plugins.jetbrains.model.rosie.RosieRuleFile;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.RosieUtils.getRosieLanguage;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of the Rosie API.
 */
public class RosieImpl implements Rosie {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final String ROSIE_POST_URL = "https://analysis.codiga.io/analyze";
    private static final Gson GSON = new Gson();

    /**
     * Current supported languages by Rosie.
     * <p>
     * See also {@link io.codiga.plugins.jetbrains.utils.RosieUtils#getRosieLanguage(LanguageEnumeration)}.
     */
    private static final List<LanguageEnumeration> SUPPORTED_LANGUAGES = List.of(LanguageEnumeration.PYTHON);

    public RosieImpl() {
        // no constructor instructions
    }

    private String getUserAgent() {
        return String.format("%s %s %s",
            ApplicationInfo.getInstance().getFullApplicationName(),
            ApplicationInfo.getInstance().getMajorVersion(),
            ApplicationInfo.getInstance().getMinorVersion());
    }

    /**
     * This is a way to debug Rosie. To enable Rosie, put a rosie.debug at the root of your project.
     * No file = no rosie
     * Then, the file should be like this
     * {
     * "rules": [
     * {
     * "id": "empty-parameters",
     * "contentBase64": "<rule-content-base64>=",
     * "language": "python",
     * "type": "ast",
     * "entityChecked": "functiondefinition"
     * },
     * {
     * "id": "timeout-request",
     * "contentBase64": "<rule-content-base64>",
     * "language": "python",
     * "type": "ast",
     * "entityChecked": "functioncall"
     * }
     * ]
     * }
     *
     * @param project
     * @return
     */
    public String getRosieDebugContent(@NotNull Project project) {
        List<VirtualFile> rootFiles = Arrays.stream(ProjectRootManager.getInstance(project)
                .getContentRoots())
            .flatMap(v -> Arrays.stream(VfsUtil.getChildren(v)))
            .collect(Collectors.toList());

        // Find package.json in these files
        Optional<VirtualFile> packageFileOptional = rootFiles.stream().filter(v -> v.getName().equalsIgnoreCase("rosie.debug")).findFirst();
        String content = null;
        if (packageFileOptional.isPresent()) {
            try {
                InputStream inputStream = packageFileOptional.get().getInputStream();
                content = new String(inputStream.readAllBytes());
                inputStream.close();
                return content;
            } catch (IOException e) {
                LOGGER.error("cannot open rosie.debug", e);
                return null;
            }
        }
        return null;
    }

    @Override
    @NotNull
    public List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project) {
        if (psiFile.getVirtualFile() == null) {
            return List.of();
        }

        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(psiFile.getVirtualFile().getCanonicalPath());

        // not supported, we exit right away
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            LOGGER.info(String.format("language not supported %s", language));
            return List.of();
        }

        try {
            String rulesString = getRosieDebugContent(project);
            if (rulesString == null) {
                return List.of();
            }
            RosieRuleFile rosieRuleFile = GSON.fromJson(rulesString, RosieRuleFile.class);
            String codeBase64 = Base64.getEncoder().encodeToString(psiFile.getText().getBytes());

            // Prepare the request
            RosieRequest request = new RosieRequest(psiFile.getName(), getRosieLanguage(language), "utf8", codeBase64, rosieRuleFile.rules, true);
            String requestString = GSON.toJson(request);
            StringEntity postingString = new StringEntity(requestString); //gson.toJson() converts your pojo to json
            HttpPost httpPost = new HttpPost(ROSIE_POST_URL);
            httpPost.addHeader("User-Agent", getUserAgent());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(postingString);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpPost);
            List<RosieAnnotation> annotations = List.of();
            if (response.getEntity() != null) {

                String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                RosieResponse rosieResponse = GSON.fromJson(result, RosieResponse.class);
                LOGGER.debug("rosie response: " + rosieResponse);

                annotations = rosieResponse.ruleResponses.stream()
                    .flatMap(res -> res.violations.stream().map(violation -> new RosieAnnotation(res.identifier, violation)))
                    .collect(toList());
            }
            client.close();
            return annotations;
        } catch (IOException unsupportedEncodingException) {
            LOGGER.error("[RosieImpl] ClientProtocolException", unsupportedEncodingException);
            return List.of();
        } catch (JsonSyntaxException jsonSyntaxException) {
            LOGGER.error("[RosieImpl] cannot decode JSON", jsonSyntaxException);
            return List.of();
        }
    }
}
