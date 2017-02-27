package org.jboss.swagger.addon.facet;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.swagger.addon.config.SwaggerConfiguration;
import org.jboss.swagger.addon.util.DependencyUtil;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * The implementation of the {@link SwaggerFacet}
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class SwaggerFacetImpl extends AbstractFacet<Project> implements SwaggerFacet {

    public static final Coordinate ANALYZER_PLUGIN_COORDINATE = CoordinateBuilder.create().setGroupId("com.sebastian-daschner").setArtifactId("jaxrs-analyzer-maven-plugin");

    public static final String ANALYZER_GOAL = "analyze-jaxrs";

    public static final String ANALYZER_PHASE = "process-test-classes";


    @Inject
    DependencyUtil dependencyUtil;

    @Inject
    private SwaggerConfiguration configuration;

    @Override
    public boolean install() {
        addAnalyzerMavenPlugin();
        return isInstalled();
    }

    /**
     * creates the plugin with default values (if it doesn't exists)
     */
    private void addAnalyzerMavenPlugin() {
        MavenPluginFacet pluginFacet = getFaceted().getFacet(MavenPluginFacet.class);

        MavenPluginBuilder analyzerBuilder;
        if (!pluginFacet.hasPlugin(ANALYZER_PLUGIN_COORDINATE)) {
            analyzerBuilder = MavenPluginBuilder.create().setCoordinate(dependencyUtil.getLatestAnalyzerVersion());
            analyzerBuilder.addExecution(ExecutionBuilder.create()
                    .addGoal("analyze-jaxrs")
                    .setPhase(ANALYZER_PHASE));
            ConfigurationElement backend = ConfigurationElementBuilder.create()
                    .createConfigurationElement("backend")
                    .setText("swagger");
            ConfigurationElement resourcesDir = ConfigurationElementBuilder.create()
                    .createConfigurationElement("resourcesDir")
                    .setText((resolveProjectName() + "/"+configuration.getResourcesDir()).replaceAll("//","/"));
            analyzerBuilder.createConfiguration()
                    .addConfigurationElement(backend)
                    .addConfigurationElement(resourcesDir);

            pluginFacet.addPlugin(analyzerBuilder);

        }

    }

    /**
     * resolves project name based on pom file based on following priority:
     *
     * build finalName > project name > artifactId
     */
    private String resolveProjectName() {
        MavenFacet mavenFacet = getFaceted().getFacet(MavenFacet.class);
        if (mavenFacet.getModel().getBuild().getFinalName() != null) {
            return mavenFacet.getModel().getBuild().getFinalName();
        } else if (mavenFacet.getModel().getName() != null) {
            return mavenFacet.getModel().getName();
        } else {
            return mavenFacet.getModel().getArtifactId();
        }
    }


    @Override
    public boolean isInstalled() {
        MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
        return facet.hasPlugin(ANALYZER_PLUGIN_COORDINATE);
    }

    @Override
    public SwaggerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(SwaggerConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    public void generateSwaggerResources() {
        MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
        maven.executeMaven(Arrays.asList(ANALYZER_PHASE));
    }

    public boolean hasSwaggerUIResources() {
        Resource<?> apiDocs = getFaceted().getRoot()
                .reify(DirectoryResource.class)
                .getChildDirectory("src").getChildDirectory("main").getChildDirectory("webapp")
                .getChildDirectory(configuration.getResourcesDir());
        return apiDocs.exists() && apiDocs.getChild("index.html").exists();
    }

}
