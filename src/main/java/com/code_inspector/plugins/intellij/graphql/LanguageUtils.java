package com.code_inspector.plugins.intellij.graphql;

import com.code_inspector.api.type.LanguageEnumeration;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;

public final class LanguageUtils {

    private static Map<String, LanguageEnumeration> EXTENSION_TO_LANGUAGE = ImmutableMap.<String, LanguageEnumeration>builder()
        .put("c", LanguageEnumeration.C)
        .put("cls", LanguageEnumeration.APEX)
        .put("cpp", LanguageEnumeration.CPP)
        .put("dart", LanguageEnumeration.DART)
        .put("php", LanguageEnumeration.PHP)
        .put("php4", LanguageEnumeration.PHP)
        .put("php5", LanguageEnumeration.PHP)
        .put("py", LanguageEnumeration.PYTHON)
        .put("py3", LanguageEnumeration.PYTHON)
        .put("rb", LanguageEnumeration.RUBY)
        .put("go", LanguageEnumeration.GO)
        .put("json", LanguageEnumeration.JSON)
        .put("java", LanguageEnumeration.JAVA)
        .put("rs", LanguageEnumeration.RUST)
        .put("kt", LanguageEnumeration.KOTLIN)
        .put("scala", LanguageEnumeration.SCALA)
        .put("sh", LanguageEnumeration.SHELL)
        .put("bash", LanguageEnumeration.SHELL)
        .put("tf", LanguageEnumeration.TERRAFORM)
        .put("ts", LanguageEnumeration.TYPESCRIPT)
        .put("js", LanguageEnumeration.JAVASCRIPT)
        .put("jsx", LanguageEnumeration.JAVASCRIPT)
        .put("yml", LanguageEnumeration.YAML)
        .put("yaml", LanguageEnumeration.YAML)
        .build();

    private LanguageUtils() {
        // do not instantiate
    }

    public static LanguageEnumeration getLanguageFromFilename(final String filename) {
        final String extension = FilenameUtils.getExtension(filename);

        if (filename.toLowerCase(java.util.Locale.getDefault()).startsWith("docker")) {
            return LanguageEnumeration.DOCKER;
        }

        return EXTENSION_TO_LANGUAGE.getOrDefault(extension, LanguageEnumeration.UNKNOWN);
    }
}
