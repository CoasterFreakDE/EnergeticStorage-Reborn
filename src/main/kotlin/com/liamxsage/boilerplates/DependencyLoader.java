package com.liamxsage.boilerplates;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage") // We keep an eye on that.
public class DependencyLoader implements PluginLoader {

    private static final Logger LOGGER = Logger.getLogger(DependencyLoader.class.getName());

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver maven = new MavenLibraryResolver();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(".dependencies"))))) {
            reader.lines().forEach(dependency -> {
                LOGGER.log(Level.INFO, "Adding dependency: " + dependency);
                maven.addDependency(new Dependency(new DefaultArtifact(dependency), null));
            });

            maven.addRepository(new RemoteRepository.Builder("flawcra", "default", "https://nexus.flawcra.cc/repository/maven-mirrors/").build());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dependencies", e);
        }

        classpathBuilder.addLibrary(maven);
    }
}
