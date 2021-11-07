package io.codiga.plugins.jetbrains.git;

import io.codiga.plugins.jetbrains.model.FileLinePair;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CodigaGitUtilsTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodigaGitUtilsTest.class);

    @Test
    public void testPatchParsingAddingLines() {
        LOGGER.info(showTestHeader("testPatchParsing"));
        myFixture.configureByFile("FileOffset.java");
        String patchContent = readFile("diff-example1.diff");
        List<TextFilePatch> patches = CodigaGitUtils.getTextFilePatchesFromDiff(patchContent);
        Map<FileLinePair, FileLinePair> indexedPatchHunks = CodigaGitUtils.indexPatchHunks(patches);

        FileLinePair flp = new FileLinePair("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", 148);
        indexedPatchHunks.forEach((c,v) -> {
            LOGGER.info(String.format("flp %s -> %s", c, v));
        });
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(145, indexedPatchHunks.get(flp).getLineNumber().intValue());

        flp = new FileLinePair("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", 151);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(149, indexedPatchHunks.get(flp).getLineNumber().intValue());
    }

    @Test
    public void testPatchParsingRemovingLines() {
        LOGGER.info(showTestHeader("testPatchParsing"));
        myFixture.configureByFile("FileOffset.java");
        String patchContent = readFile("diff-example1.diff");
        List<TextFilePatch> patches = CodigaGitUtils.getTextFilePatchesFromDiff(patchContent);
        Map<FileLinePair, FileLinePair> indexedPatchHunks = CodigaGitUtils.indexPatchHunks(patches);

        FileLinePair flp = new FileLinePair("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", 3);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(5, indexedPatchHunks.get(flp).getLineNumber().intValue());

        flp = new FileLinePair("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", 9);
        assertTrue(indexedPatchHunks.containsKey(flp));
        assertEquals("src/main/java/io/codiga/plugins/jetbrains/git/CodigaGitUtils.java", indexedPatchHunks.get(flp).getFilename());
        assertEquals(8, indexedPatchHunks.get(flp).getLineNumber().intValue());
    }
}
