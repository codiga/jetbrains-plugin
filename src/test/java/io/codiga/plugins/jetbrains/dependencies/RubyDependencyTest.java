package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class RubyDependencyTest extends TestBase {

    RubyDependency rubyDependency = new RubyDependency();

    @Test
    public void testParseRequirements1() throws IOException {
        FileInputStream fileInputStream = getInputStream("gemfile-example1");
        List<Dependency> dependencies = rubyDependency.getDependenciesFromInputStream(fileInputStream);
        dependencies.forEach(d -> System.out.println(d.getName()));
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("sqlite3")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("rails")));
        Assertions.assertFalse(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("unicorn")));
        Assertions.assertFalse(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("capistrano")));
        fileInputStream.close();
    }

    @Test
    public void testParseRequirements2() throws IOException {
        FileInputStream fileInputStream = getInputStream("gemfile-example2");
        List<Dependency> dependencies = rubyDependency.getDependenciesFromInputStream(fileInputStream);
        dependencies.forEach(d -> System.out.println(d.getName()));
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("nokogiri")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("rails")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("rack")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("thin")));
        fileInputStream.close();
    }

    @Test
    public void testParseRequirements3() throws IOException {
        FileInputStream fileInputStream = getInputStream("gemfile-example3");
        List<Dependency> dependencies = rubyDependency.getDependenciesFromInputStream(fileInputStream);
        dependencies.forEach(d -> System.out.println(d.getName()));
        Assertions.assertFalse(dependencies.isEmpty());
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("sass-rails")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("rspec-rails")));
        Assertions.assertTrue(dependencies.stream().anyMatch(d -> d.getName().equalsIgnoreCase("omniauth")));
        fileInputStream.close();
    }

    @Test
    public void testParseRequirementsInvalid() throws IOException {
        FileInputStream fileInputStream = getInputStream("gemfile-invalid");
        List<Dependency> dependencies = rubyDependency.getDependenciesFromInputStream(fileInputStream);
        dependencies.forEach(d -> System.out.println(d.getName()));
        Assertions.assertTrue(dependencies.isEmpty());
        fileInputStream.close();
    }

}
