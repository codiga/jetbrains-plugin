package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class PhpDependencyTest extends TestBase {
    PhpDependency phpDependency = new PhpDependency();

    @Test
    public void testComposer1() throws IOException {
        FileInputStream fileInputStream = getInputStream("composer1.json");
        List<Dependency> dependencies = phpDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("acquia/blt-require-dev")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("drupal/devel")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("php")));
        fileInputStream.close();
    }

    @Test
    public void testComposer2() throws IOException {
        FileInputStream fileInputStream = getInputStream("composer2.json");
        List<Dependency> dependencies = phpDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("composer/installers")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("behat/mink")));
        fileInputStream.close();
    }

    @Test
    public void testComposerInvalid() throws IOException {
        FileInputStream fileInputStream = getInputStream("composer-invalid.json");
        List<Dependency> dependencies = phpDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertTrue(dependencies.isEmpty());
        fileInputStream.close();
    }

    @Test
    public void testComposerWithoutRequire() throws IOException {
        FileInputStream fileInputStream = getInputStream("composer-without-require.json");
        List<Dependency> dependencies = phpDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertTrue(dependencies.isEmpty());
        fileInputStream.close();
    }
}
