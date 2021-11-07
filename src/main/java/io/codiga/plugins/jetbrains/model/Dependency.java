package io.codiga.plugins.jetbrains.model;

import java.util.Optional;

/**
 * Represents a Dependency that is being used by the IDE
 * plugins.
 */
public class Dependency {

    private String name;
    private Optional<String> version;


    public Dependency(String name, Optional<String> version) {
        this.name = name;
        this.version = version;
    }

    public Optional<String> getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }
}
