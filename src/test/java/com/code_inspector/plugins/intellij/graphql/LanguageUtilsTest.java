package com.code_inspector.plugins.intellij.graphql;


import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;


public class LanguageUtilsTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testAllLanguages() {
        assertEquals(LanguageEnumeration.KOTLIN, getLanguageFromFilename("myfile.kt"));
        assertEquals(LanguageEnumeration.APEX, getLanguageFromFilename("myfile.cls"));
        assertEquals(LanguageEnumeration.C, getLanguageFromFilename("myfile.c"));
        assertEquals(LanguageEnumeration.CPP, getLanguageFromFilename("myfile.cpp"));
        assertEquals(LanguageEnumeration.DOCKER, getLanguageFromFilename("Dockerfile"));
        assertEquals(LanguageEnumeration.YAML, getLanguageFromFilename("myfile.yaml"));
        assertEquals(LanguageEnumeration.YAML, getLanguageFromFilename("myfile.yml"));
        assertEquals(LanguageEnumeration.GO, getLanguageFromFilename("myfile.go"));
        assertEquals(LanguageEnumeration.JAVA, getLanguageFromFilename("myfile.java"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, getLanguageFromFilename("myfile.js"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, getLanguageFromFilename("myfile.jsx"));
        assertEquals(LanguageEnumeration.TYPESCRIPT, getLanguageFromFilename("myfile.ts"));
        assertEquals(LanguageEnumeration.DART, getLanguageFromFilename("myfile.dart"));
        assertEquals(LanguageEnumeration.TERRAFORM, getLanguageFromFilename("myfile.tf"));
        assertEquals(LanguageEnumeration.SCALA, getLanguageFromFilename("myfile.scala"));
        assertEquals(LanguageEnumeration.RUST, getLanguageFromFilename("myfile.rs"));
        assertEquals(LanguageEnumeration.RUBY, getLanguageFromFilename("myfile.rb"));
        assertEquals(LanguageEnumeration.SHELL, getLanguageFromFilename("myfile.sh"));
        assertEquals(LanguageEnumeration.SHELL, getLanguageFromFilename("myfile.bash"));
        assertEquals(LanguageEnumeration.JSON, getLanguageFromFilename("myfile.json"));
        assertEquals(LanguageEnumeration.UNKNOWN, getLanguageFromFilename("myfile"));
    }
}
