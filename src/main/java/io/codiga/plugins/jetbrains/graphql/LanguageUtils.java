package io.codiga.plugins.jetbrains.graphql;

import io.codiga.api.type.LanguageEnumeration;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;

public final class LanguageUtils {

    private final static Map<String, LanguageEnumeration> EXTENSION_TO_LANGUAGE = ImmutableMap.<String, LanguageEnumeration>builder()
        .put("bash", LanguageEnumeration.SHELL)
        .put("c", LanguageEnumeration.C)
        .put("cls", LanguageEnumeration.APEX)
        .put("cfc", LanguageEnumeration.COLDFUSION)
        .put("cfm", LanguageEnumeration.COLDFUSION)
        .put("cpp", LanguageEnumeration.CPP)
        .put("css", LanguageEnumeration.CSS)
        .put("cs", LanguageEnumeration.CSHARP)
        .put("dart", LanguageEnumeration.DART)
        .put("go", LanguageEnumeration.GO)
        .put("hs", LanguageEnumeration.HASKELL)
        .put("html", LanguageEnumeration.HTML)
        .put("html5", LanguageEnumeration.HTML)
        .put("htm", LanguageEnumeration.HTML)
        .put("ipynb", LanguageEnumeration.PYTHON)
        .put("js", LanguageEnumeration.JAVASCRIPT)
        .put("jsx", LanguageEnumeration.JAVASCRIPT)
        .put("json", LanguageEnumeration.JSON)
        .put("java", LanguageEnumeration.JAVA)
        .put("kt", LanguageEnumeration.KOTLIN)
        .put("m", LanguageEnumeration.OBJECTIVEC)
        .put("md", LanguageEnumeration.MARKDOWN)
        .put("mm", LanguageEnumeration.OBJECTIVEC)
        .put("M", LanguageEnumeration.OBJECTIVEC)
        .put("pl", LanguageEnumeration.PERL)
        .put("pm", LanguageEnumeration.PERL)
        .put("py", LanguageEnumeration.PYTHON)
        .put("py3", LanguageEnumeration.PYTHON)
        .put("rb", LanguageEnumeration.RUBY)
        .put("rs", LanguageEnumeration.RUST)
        .put("rhtml", LanguageEnumeration.RUBY)
        .put("php", LanguageEnumeration.PHP)
        .put("php4", LanguageEnumeration.PHP)
        .put("php5", LanguageEnumeration.PHP)
        .put("sass", LanguageEnumeration.SASS)
        .put("scala", LanguageEnumeration.SCALA)
        .put("scss", LanguageEnumeration.SCSS)
        .put("sql", LanguageEnumeration.SQL)
        .put("sol", LanguageEnumeration.SOLIDITY)
        .put("sh", LanguageEnumeration.SHELL)
        .put("swift", LanguageEnumeration.SWIFT)
        .put("tf", LanguageEnumeration.TERRAFORM)
        .put("ts", LanguageEnumeration.TYPESCRIPT)
        .put("tsx", LanguageEnumeration.TYPESCRIPT)
        .put("twig", LanguageEnumeration.TWIG)

        .put("yml", LanguageEnumeration.YAML)
        .put("yaml", LanguageEnumeration.YAML)
        .build();

    private LanguageUtils() {
        // do not instantiate
    }

    public static LanguageEnumeration getLanguageFromFilename(final String filename) {
        String extension;
        try {
            extension = FilenameUtils.getExtension(filename);
        } catch (IllegalArgumentException iae) {
            return LanguageEnumeration.UNKNOWN;
        }

        if (filename.toLowerCase(java.util.Locale.getDefault()).startsWith("docker")) {
            return LanguageEnumeration.DOCKER;
        }

        return EXTENSION_TO_LANGUAGE.getOrDefault(extension, LanguageEnumeration.UNKNOWN);
    }
}
