package io.codiga.plugins.jetbrains.utils;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.actions.snippet_search.service.DefaultFileTypeService;
import io.codiga.plugins.jetbrains.actions.snippet_search.service.JavaFileTypeService;
import io.codiga.plugins.jetbrains.actions.snippet_search.service.PythonFileTypeService;
import io.codiga.plugins.jetbrains.actions.snippet_search.service.SyntaxHighlightFileTypeService;
import io.codiga.plugins.jetbrains.actions.snippet_search.service.YamlFileTypeService;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LanguageUtils {

    /**
     * Used for providing file types and related information for code syntax highlighting.
     *
     * @see io.codiga.plugins.jetbrains.actions.snippet_search.SnippetPanel
     */
    private static final Map<LanguageEnumeration, Class<? extends SyntaxHighlightFileTypeService>> LANGUAGE_TO_SERVICE = Map.of(
        LanguageEnumeration.JAVA, JavaFileTypeService.class,
        LanguageEnumeration.PYTHON, PythonFileTypeService.class,
        LanguageEnumeration.YAML, YamlFileTypeService.class
    );

    /**
     * Keywords here are specified as lowercase values, and compared against the lowercase version of comment lines.
     */
    private static final Set<String> KEYWORDS_TO_FILTER_OUT = Set.of("todo", "fixme");

    private static final Map<String, LanguageEnumeration> EXTENSION_TO_LANGUAGE = ImmutableMap.<String, LanguageEnumeration>builder()
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
    }

    public static String getLanguageName(LanguageEnumeration languageEnumeration) {
        switch (languageEnumeration) {
            case HASKELL:
                return "Haskell";
            case COLDFUSION:
                return "Coldfusion";
            case HTML:
                return "HTML";
            case DOCKER:
                return "Docker";
            case TERRAFORM:
                return "Terraform";
            case JSON:
                return "Json";
            case YAML:
                return "YAML";
            case TYPESCRIPT:
                return "TypeScript";
            case JAVASCRIPT:
                return "JavaScript";
            case SQL:
                return "SQL";
            case SHELL:
                return "Shell";
            case SCALA:
                return "Scala";
            case RUST:
                return "Rust";
            case RUBY:
                return "Ruby";
            case OBJECTIVEC:
                return "Objective-C";
            case PHP:
                return "PHP";
            case PYTHON:
                return "Python";
            case CSHARP:
                return "C#";
            case DART:
                return "Dart";
            case CSS:
                return "CSS";
            case CPP:
                return "C++";
            case C:
                return "C";
            case APEX:
                return "Apex";
            case GO:
                return "Go";
            case JAVA:
                return "Java";
            case MARKDOWN:
                return "Markdown";
            case SCSS:
                return "Scss";
            case SASS:
                return "Sass";
            case TWIG:
                return "Twig";
            default:
                return "Unknown";
        }
    }

    /**
     * Indicate if a line of code starts as a comment.
     *
     * @param languageEnumeration
     * @param line
     * @return
     */
    public static boolean lineStartsWithComment(@NotNull LanguageEnumeration languageEnumeration, @NotNull String line) {
        String filteredLine = line.replaceAll(" ", "");
        switch (languageEnumeration) {
            case JAVASCRIPT:
            case TYPESCRIPT:
            case C:
            case APEX:
            case CPP:
            case SCALA:
            case DART:
            case GO:
            case OBJECTIVEC:
            case KOTLIN:
            case JAVA:
            case SWIFT:
            case SOLIDITY:
            case RUST:
            case SCSS:
            case SASS:
                return filteredLine.startsWith("//");
            case PYTHON:
            case SHELL:
            case PERL:
            case YAML:
                return filteredLine.startsWith("#");
            case TERRAFORM:
            case PHP:
                return filteredLine.startsWith("#") || filteredLine.startsWith("//");
            case COLDFUSION:
                return filteredLine.startsWith("<!---");
            case HASKELL:
                return filteredLine.startsWith("--");
            case CSS:
                return filteredLine.startsWith("/*");
            case TWIG:
                return filteredLine.startsWith("{#");
            default:
                return false;
        }
    }

    /**
     * Returns the one-line comment prefix for the argument language.
     */
    @NotNull
    public static String commentPrefixFor(@NotNull LanguageEnumeration languageEnumeration) {
        switch (languageEnumeration) {
            case JAVASCRIPT:
            case TYPESCRIPT:
            case C:
            case APEX:
            case CPP:
            case SCALA:
            case DART:
            case GO:
            case OBJECTIVEC:
            case KOTLIN:
            case JAVA:
            case SWIFT:
            case SOLIDITY:
            case RUST:
            case SCSS:
            case SASS:
                return "//";
            case PYTHON:
            case SHELL:
            case PERL:
            case YAML:
            case TERRAFORM:
            case PHP:
                return "#";
            case COLDFUSION:
                return "<!---";
            case HASKELL:
                return "--";
            case CSS:
                return "/*";
            case TWIG:
                return "{#";
            default:
                return "//";
        }
    }

    public static @NotNull String removeLineFromCommentsSymbols(@NotNull String line) {
        return line.replaceAll("#", "").replaceAll("//", "");
    }

    public static long numberOfWordsInComment(@NotNull String line) {
        return Arrays.stream(removeLineFromCommentsSymbols(line).split(" ")).filter(s -> !s.isEmpty()).count();
    }

    /**
     * Returns whether the argument line contains lower-case any of the {@link #KEYWORDS_TO_FILTER_OUT}.
     */
    public static boolean containsTodoKeyword(@NotNull String line) {
        return KEYWORDS_TO_FILTER_OUT.stream().anyMatch(keyword -> line.toLowerCase().contains(keyword));
    }

    /**
     * Returns the {@link FileType} associated with the argument language.
     * <p>
     * It looks up the appropriate file type provider application service, and returns its file type. If no service is
     * found for the given language, it falls back to {@link DefaultFileTypeService}.
     */
    @NotNull
    public static FileType getFileTypeForLanguage(LanguageEnumeration language) {
        return Optional.ofNullable(LANGUAGE_TO_SERVICE.get(language))
            .map(cls -> ApplicationManager.getApplication().getService(cls))
            .map(SyntaxHighlightFileTypeService::getFileType)
            .orElseGet(() -> ApplicationManager.getApplication().getService(DefaultFileTypeService.class).getFileType());
    }

    /**
     * Returns the language based on the argument file name.
     *
     * @param filename the file name, including its extension
     * @return the language if supported, {@code LanguageEnumeration.DOCKER} if the file name starts with "docker",
     * or equals to "dockerfile", or {@code LanguageEnumeration.UNKNOWN} if the file extension is not available or not supported
     */
    public static LanguageEnumeration getLanguageFromFilename(final String filename) {
        String extension;
        try {
            extension = FilenameUtils.getExtension(filename);
        } catch (IllegalArgumentException iae) {
            return LanguageEnumeration.UNKNOWN;
        }

        if (filename.toLowerCase().startsWith("docker") || extension.equals("dockerfile")) {
            return LanguageEnumeration.DOCKER;
        }

        return EXTENSION_TO_LANGUAGE.getOrDefault(extension, LanguageEnumeration.UNKNOWN);
    }

    /**
     * Returns the language for the argument file extension.
     *
     * @param extension The file extension. Should be specified without the leading dot, e.g. "py", "yaml".
     * @return the language, or {@code LanguageEnumeration.UNKNOWN} if the file extension is not supported
     */
    public static LanguageEnumeration getLanguageFromExtension(String extension) {
        return EXTENSION_TO_LANGUAGE.getOrDefault(extension, LanguageEnumeration.UNKNOWN);
    }
}
