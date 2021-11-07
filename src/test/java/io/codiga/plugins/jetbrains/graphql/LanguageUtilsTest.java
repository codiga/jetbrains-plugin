package io.codiga.plugins.jetbrains.graphql;


import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.git.CodigaGitUtilsTest;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LanguageUtilsTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodigaGitUtilsTest.class);

    @Test
    public void testAllLanguages() {
        assertEquals(LanguageEnumeration.KOTLIN, LanguageUtils.getLanguageFromFilename("myfile.kt"));
        assertEquals(LanguageEnumeration.APEX, LanguageUtils.getLanguageFromFilename("myfile.cls"));
        assertEquals(LanguageEnumeration.C, LanguageUtils.getLanguageFromFilename("myfile.c"));
        assertEquals(LanguageEnumeration.CPP, LanguageUtils.getLanguageFromFilename("myfile.cpp"));
        assertEquals(LanguageEnumeration.DOCKER, LanguageUtils.getLanguageFromFilename("Dockerfile"));
        assertEquals(LanguageEnumeration.YAML, LanguageUtils.getLanguageFromFilename("myfile.yaml"));
        assertEquals(LanguageEnumeration.YAML, LanguageUtils.getLanguageFromFilename("myfile.yml"));
        assertEquals(LanguageEnumeration.GO, LanguageUtils.getLanguageFromFilename("myfile.go"));
        assertEquals(LanguageEnumeration.JAVA, LanguageUtils.getLanguageFromFilename("myfile.java"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, LanguageUtils.getLanguageFromFilename("myfile.js"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, LanguageUtils.getLanguageFromFilename("myfile.jsx"));
        assertEquals(LanguageEnumeration.TYPESCRIPT, LanguageUtils.getLanguageFromFilename("myfile.ts"));
        assertEquals(LanguageEnumeration.DART, LanguageUtils.getLanguageFromFilename("myfile.dart"));
        assertEquals(LanguageEnumeration.TERRAFORM, LanguageUtils.getLanguageFromFilename("myfile.tf"));
        assertEquals(LanguageEnumeration.SCALA, LanguageUtils.getLanguageFromFilename("myfile.scala"));
        assertEquals(LanguageEnumeration.RUST, LanguageUtils.getLanguageFromFilename("myfile.rs"));
        assertEquals(LanguageEnumeration.RUBY, LanguageUtils.getLanguageFromFilename("myfile.rb"));
        assertEquals(LanguageEnumeration.SHELL, LanguageUtils.getLanguageFromFilename("myfile.sh"));
        assertEquals(LanguageEnumeration.SHELL, LanguageUtils.getLanguageFromFilename("myfile.bash"));
        assertEquals(LanguageEnumeration.JSON, LanguageUtils.getLanguageFromFilename("myfile.json"));
        assertEquals(LanguageEnumeration.UNKNOWN, LanguageUtils.getLanguageFromFilename("myfile"));
    }
}
