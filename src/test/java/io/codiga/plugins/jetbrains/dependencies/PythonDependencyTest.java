package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class PythonDependencyTest extends TestBase {

    PythonDependency pythonDependency = new PythonDependency();

    @Test
    public void testParseRequirements1Valid() throws IOException {
        FileInputStream fileInputStream = getInputStream("requirements1.txt");
        List<Dependency> dependencies = pythonDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("cryptography")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("mock")));
        Assertions.assertFalse(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("pandas")));
        fileInputStream.close();
    }

    @Test
    public void testParseRequirements2Valid() throws IOException {
        FileInputStream fileInputStream = getInputStream("requirements2.txt");
        List<Dependency> dependencies = pythonDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("tqdm")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("requests")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("sklearn")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("python-dateutil")));
        Assertions.assertFalse(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("pandas")));
        fileInputStream.close();
    }

    @Test
    public void testParseRequirementsInvalid() throws IOException {
        FileInputStream fileInputStream = getInputStream("requirements-invalid.txt");
        List<Dependency> dependencies = pythonDependency.getDependenciesFromInputStream(fileInputStream);
        Assertions.assertTrue(dependencies.isEmpty());
        fileInputStream.close();
    }
}
