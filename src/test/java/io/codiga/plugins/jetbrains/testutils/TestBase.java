package io.codiga.plugins.jetbrains.testutils;

import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Base test class for integration tests.
 */
public abstract class TestBase extends BasePlatformTestCase {

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
        String communityPath = PlatformTestUtil.getCommunityPath();
        String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        if (communityPath.startsWith(homePath)) {
            return communityPath.substring(homePath.length()) + getTestDataRelativePath();
        }
        return getTestDataRelativePath();
    }

    /**
     * Returns the path of the test data folder relative to the project root.
     * <p>
     * Override in test classes if you work with another test data folder.
     */
    protected String getTestDataRelativePath() {
        return "src/test/data";
    }

}
