package io.codiga.plugins.jetbrains.services;

import com.google.gson.Gson;
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

public class RosieImpl implements Rosie {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final String ROSIE_POST_URL = "https://analysis.codiga.io/analyze";

    // Current supported languages by Rosie
    private static final List<LanguageEnumeration> SUPPORTED_LANGUAGES = List.of(LanguageEnumeration.PYTHON);

    public RosieImpl() {
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
     * "contentBase64": "ICAgICAgICBmdW5jdGlvbiB2aXNpdChub2RlKSB7CiAgICAgICAgICAgIGNvbnN0IHBhcmFtZXRlcnNXaXRoRW1wdHlBcnJheSA9IG5vZGUucGFyYW1ldGVycy52YWx1ZXMuZmlsdGVyKHAgPT4gcCAmJiBwLmRlZmF1bHRWYWx1ZSAmJiBwLmRlZmF1bHRWYWx1ZS52YWx1ZSA9PT0gIltdIik7CgogICAgICAgICAgICBmb3IodmFyIGkgPSAwIDsgaSA8IHBhcmFtZXRlcnNXaXRoRW1wdHlBcnJheS5sZW5ndGggOyBpKyspIHsKICAgICAgICAgICAgICAgIGNvbnN0IHBhcmFtZXRlciA9IHBhcmFtZXRlcnNXaXRoRW1wdHlBcnJheVtpXTsKICAgICAgICAgICAgICAgIGNvbnNvbGUubG9nKHBhcmFtZXRlci5uYW1lLnZhbHVlKTsKICAgICAgICAgICAgICAgIGNvbnNvbGUubG9nKHBhcmFtZXRlci5zdGFydC5jb2wpOwogICAgICAgICAgICAgICAgY29uc3QgZXJyb3IgPSBidWlsZEVycm9yKHBhcmFtZXRlci5kZWZhdWx0VmFsdWUuc3RhcnQubGluZSwgcGFyYW1ldGVyLmRlZmF1bHRWYWx1ZS5zdGFydC5jb2wsIHBhcmFtZXRlci5kZWZhdWx0VmFsdWUuZW5kLmxpbmUsIHBhcmFtZXRlci5kZWZhdWx0VmFsdWUuZW5kLmNvbCwgImNhbm5vdCB1c2UgZGVmYXVsdCBpbml0aWFsaXplciBbXSBpbiBmdW5jdGlvbiIsICJDUklUSUNBTCIsICJTQUZFVFkiKTsKICAgICAgICAgICAgICAgIGFkZEVycm9yKGVycm9yKTsKICAgICAgICAgICAgfQogICAgICAgIH0=",
     * "language": "python",
     * "type": "ast",
     * "entityChecked": "functiondefinition"
     * },
     * {
     * "id": "timeout-request",
     * "contentBase64": "ICAgICAgICBmdW5jdGlvbiB2aXNpdChub2RlKSB7CiAgICAgICAgICAgIG5vZGUuYXJndW1lbnRzLnZhbHVlcy5maWx0ZXIoYSA9PiBhLm5hbWUpLmZvckVhY2goYSA9PiBjb25zb2xlLmxvZyhhLm5hbWUudmFsdWUpKTsKICAgICAgICAgICAgY29uc3QgaGFzVGltZW91dCA9IG5vZGUuYXJndW1lbnRzLnZhbHVlcy5maWx0ZXIoYSA9PiBhLm5hbWUgJiYgYS5uYW1lLnZhbHVlID09ICJ0aW1lb3V0IikubGVuZ3RoID4gMDsKICAgICAgICAgICAgY29uc3QgYXJndW1lbnRzID0gbm9kZS5hcmd1bWVudHMudmFsdWVzOwogICAgICAgICAgICBjb25zdCBuYkFyZ3VtZW50cyA9IG5vZGUuYXJndW1lbnRzLnZhbHVlcy5sZW5ndGg7CiAgICAgICAgICAgIGNvbnN0IGFsbFBhY2thZ2VzID0gbm9kZS5nZXRJbXBvcnRzKCkuZmxhdE1hcChpID0+IGkucGFja2FnZXMubWFwKHAgPT4gcC5uYW1lLnN0cikpOwogICAgICAgICAgICBjb25zdCB1c2VSZXF1ZXN0c1BhY2thZ2UgPSBhbGxQYWNrYWdlcy5maWx0ZXIoaSA9PiBpID09PSAicmVxdWVzdHMiKS5sZW5ndGggPiAwOwogICAgICAgICAgICBjb25zb2xlLmxvZygiSEFTIFRJTUVPVVQ6IitoYXNUaW1lb3V0KTsKICAgICAgICAgICAgaWYoIWhhc1RpbWVvdXQgJiYgdXNlUmVxdWVzdHNQYWNrYWdlICYmIG5vZGUuZnVuY3Rpb25OYW1lLnZhbHVlID09PSAiZ2V0IiAmJiBub2RlLm1vZHVsZU9yT2JqZWN0LnZhbHVlID09PSAicmVxdWVzdHMiKXsKICAgICAgICAgICAgICAgIGNvbnN0IGVycm9yID0gYnVpbGRFcnJvcihub2RlLnN0YXJ0LmxpbmUsIG5vZGUuc3RhcnQuY29sLCBub2RlLmVuZC5saW5lLCBub2RlLmVuZC5jb2wsICJ0aW1lb3V0IG5vdCBkZWZpbmVkIiwgIkNSSVRJQ0FMIiwgIlNBRkVUWSIpOwogICAgICAgICAgICAgICAgY29uc3QgbGluZVRvSW5zZXJ0ID0gYXJndW1lbnRzW2FyZ3VtZW50cy5sZW5ndGggLSAxXS5lbmQubGluZTsKICAgICAgICAgICAgICAgIGNvbnN0IGNvbFRvSW5zZXJ0ID0gYXJndW1lbnRzW2FyZ3VtZW50cy5sZW5ndGggLSAxXS5lbmQuY29sICsgMTsKICAgICAgICAgICAgICAgIGNvbnN0IGVkaXQgPSBidWlsZEVkaXRBZGQobGluZVRvSW5zZXJ0LCBjb2xUb0luc2VydCwgIiwgdGltZW91dD01IikKICAgICAgICAgICAgICAgIGNvbnN0IGZpeCA9IGJ1aWxkRml4KCJhZGQgdGltZW91dCBhcmd1bWVudCIsIFtlZGl0XSk7CiAgICAgICAgICAgICAgICBhZGRFcnJvcihlcnJvci5hZGRGaXgoZml4KSk7CiAgICAgICAgICAgIH0KICAgICAgICB9",
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
        if (packageFileOptional.isPresent()) {
            InputStream inputStream = null;
            try {
                inputStream = packageFileOptional.get().getInputStream();
                String content = new String(inputStream.readAllBytes());
                inputStream.close();
                return content;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project) {
        List<RosieAnnotation> annotations = List.of();
        Gson gson = new Gson();

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
            RosieRuleFile rosieRuleFile = gson.fromJson(rulesString, RosieRuleFile.class);
            String codeBase64 = Base64.getEncoder().encodeToString(psiFile.getText().getBytes());

            // Preapare the reqeust
            RosieRequest request = new RosieRequest(psiFile.getName(), getRosieLanguage(language), "utf8", codeBase64, rosieRuleFile.rules, true);
            String requestString = gson.toJson(request);
            StringEntity postingString = new StringEntity(requestString);//gson.tojson() converts your pojo to json
            HttpPost httpPost = new HttpPost(ROSIE_POST_URL);
            httpPost.addHeader("User-Agent", getUserAgent());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(postingString);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpPost);
            if (response.getEntity() != null) {

                String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                RosieResponse rosieResponse = gson.fromJson(result, RosieResponse.class);
                LOGGER.debug("rosie response: " + rosieResponse);

                annotations = rosieResponse.ruleResponses.stream().flatMap(res -> res.violations.stream().map(violation -> new RosieAnnotation(
                    res.identifier,
                    violation.message,
                    violation.severity,
                    violation.category,
                    violation.start,
                    violation.end
                ))).collect(Collectors.toList());
            }
            client.close();
            return annotations;
        } catch (IOException unsupportedEncodingException) {
            LOGGER.error("[RosieImpl] ClientProtocolException", unsupportedEncodingException);
            return List.of();
        } catch (com.google.gson.JsonSyntaxException jsonSyntaxException) {
            LOGGER.error("[RosieImpl] cannot decode JSON", jsonSyntaxException);
            return List.of();
        }
    }
}
