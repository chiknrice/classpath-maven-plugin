/*
 * Copyright (c) 2016 Ian Bondoc
 *
 * This file is part of classpath-maven-plugin
 *
 * classpath-maven-plugin is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or(at your option) any
 * later version.
 *
 * classpath-maven-plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.chiknrice.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;

import static java.lang.String.format;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * A maven plugin that packages a file containing the classpath for the project including the project artifact.  The
 * plugin can generate classpath based on scope.  The file and path separators are also configurable but defaults to
 * platform specific if not specified.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
@Mojo(name = "jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageClasspathMojo extends AbstractMojo {

    @Parameter(required = true)
    private String classpathFilename;

    @Parameter(defaultValue = "runtime")
    private String scope;

    @Parameter
    private String fileSeparator;

    @Parameter
    private String pathSeparator;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private String targetDir;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private String classesDir;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository local;

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (fileSeparator == null) {
            fileSeparator = File.separator;
        }
        if (pathSeparator == null) {
            pathSeparator = File.pathSeparator;
        }

        String outputFilePath = format("%s/%s", classesDir, classpathFilename);
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version("2.10")
                ),
                goal("build-classpath"),
                configuration(
                        element("includeScope", scope),
                        element("fileSeparator", fileSeparator),
                        element("pathSeparator", pathSeparator),
                        element("attach", "true")
                ),
                executionEnvironment(mavenProject, mavenSession, pluginManager));

        File outputFile = new File(outputFilePath);
        File tmpFile = new File(format("%s%s%s", targetDir, File.separator, "classpath"));
        try (BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {

            String projectArtifactPath = local.pathOf(mavenProject.getArtifact());
            if (!projectArtifactPath.contains(fileSeparator)) {
                if ("/".equals(fileSeparator)) {
                    projectArtifactPath = projectArtifactPath.replace('\\', '/');
                } else {
                    projectArtifactPath = projectArtifactPath.replace('/', '\\');
                }
            }
            writer.write(format("${M2_REPO}%s%s:", fileSeparator, projectArtifactPath));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-jar-plugin"),
                        version("2.6")
                ),
                goal("jar"),
                configuration(
                        element("classifier", "classpath"),
                        element("archive", element("addMavenDescriptor", "false")),
                        element("includes", element("include", classpathFilename))
                ),
                executionEnvironment(mavenProject, mavenSession, pluginManager));
    }

}
