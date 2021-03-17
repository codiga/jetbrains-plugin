package com.code_inspector.plugins.intellij.git;

import com.code_inspector.plugins.intellij.model.FileLinePair;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.getTextFilePatchesFromDiff;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.indexPatchHunks;

public class CodeInspectorGitUtilsTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testPatchParsingAddingLines() {
        LOGGER.info(showTestHeader("testPatchParsing"));
        myFixture.configureByFile("FileOffset.java");
        String patchContent = readFile("diff-example1.diff");
        List<TextFilePatch> patches = getTextFilePatchesFromDiff(patchContent);
        Map<FileLinePair, FileLinePair> indexedPatchHunks = indexPatchHunks(patches);

        FileLinePair flp = new FileLinePair("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", 148);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(145, indexedPatchHunks.get(flp).getLineNumber().intValue());

        flp = new FileLinePair("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", 151);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(149, indexedPatchHunks.get(flp).getLineNumber().intValue());
    }

    @Test
    public void testPatchParsingRemovingLines() {
        LOGGER.info(showTestHeader("testPatchParsing"));
        myFixture.configureByFile("FileOffset.java");
        String patchContent = readFile("diff-example1.diff");
        List<TextFilePatch> patches = getTextFilePatchesFromDiff(patchContent);
        Map<FileLinePair, FileLinePair> indexedPatchHunks = indexPatchHunks(patches);

        FileLinePair flp = new FileLinePair("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", 3);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(5, indexedPatchHunks.get(flp).getLineNumber().intValue());

        flp = new FileLinePair("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", 9);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/com/code_inspector/plugins/intellij/git/CodeInspectorGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(8, indexedPatchHunks.get(flp).getLineNumber().intValue());
    }
}
