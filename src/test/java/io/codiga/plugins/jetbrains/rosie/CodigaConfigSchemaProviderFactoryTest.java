package io.codiga.plugins.jetbrains.rosie;

import com.intellij.openapi.vfs.VirtualFile;
import io.codiga.plugins.jetbrains.testutils.TestBase;

/**
 * Integration test for {@link CodigaConfigSchemaProviderFactory}.
 */
public class CodigaConfigSchemaProviderFactoryTest extends TestBase {

    @Override
    protected String getTestDataPath() {
        return TEST_DATA_BASE_PATH + "/schema";
    }

    public void testNotAvailableInNonMappingFile() {
        VirtualFile file = myFixture.copyFileToProject("non-codiga.yml");
        boolean isAvailable = new CodigaConfigSchemaProviderFactory.CodigaConfigFileSchemaProvider().isAvailable(file);

        assertFalse(isAvailable);
    }

    public void testIsAvailableInMappingFile() {
        VirtualFile file = myFixture.copyFileToProject("codiga.yml");
        boolean isAvailable = new CodigaConfigSchemaProviderFactory.CodigaConfigFileSchemaProvider().isAvailable(file);

        assertTrue(isAvailable);
    }
}
