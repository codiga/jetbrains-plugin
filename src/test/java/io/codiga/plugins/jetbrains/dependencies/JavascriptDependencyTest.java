package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;



public class JavascriptDependencyTest extends TestBase {
    JavascriptDependency javascriptDependency = new JavascriptDependency();

    @Test
    public void testParsePackageFrontendJsonValid() throws IOException {
        FileInputStream fileInputStream = getInputStream("package-frontend.json");
        List<Dependency> dependencies = javascriptDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("react")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("@apollo/client")));
        fileInputStream.close();
    }


    @Test
    public void testParsePackageInvalid() throws IOException{
        FileInputStream fileInputStream = getInputStream("package-invalid.json");

        List<Dependency> dependencies = javascriptDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertTrue(dependencies.isEmpty());

        fileInputStream.close();
    }

    @Test
    public void testParsePackageWithoutDependencies() throws IOException{
        FileInputStream fileInputStream = getInputStream("package-without-dependencies.json");
        List<Dependency> dependencies = javascriptDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertTrue(dependencies.isEmpty());

        fileInputStream.close();
    }
}
