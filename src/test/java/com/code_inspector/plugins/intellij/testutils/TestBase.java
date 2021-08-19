package com.code_inspector.plugins.intellij.testutils;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Ignore;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Arrays;

@Ignore
public class TestBase extends BasePlatformTestCase {

    public TestBase() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public final String readFile(String path){
        String completePath = this.getTestDataPath() + "/" + path;

        try {
            return new String(Files.readAllBytes(new File(completePath).toPath()));
        }

        catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }

    public final FileInputStream getInputStream(String path){
        String completePath = this.getTestDataPath() + "/" + path;

        try {
            return new FileInputStream(completePath);
        }

        catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }

    protected String showTestHeader(String title) {
        int n = (60 - title.length()) / 2;
        char[] marks = new char[n];
        Arrays.fill(marks, '=');
        return new String(marks) + " " + title + " " +  new String(marks);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/data";
    }
}
