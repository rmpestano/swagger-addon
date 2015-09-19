package com.tdc.addon.swagger.facet;

import com.tdc.addon.swagger.config.SwaggerConfiguration;
import java.util.Iterator;
import java.util.Properties;
import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPlugin;
import org.jboss.forge.addon.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link WildflySwarmFacet}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class SwaggerFacetImpl extends AbstractFacet<Project> implements
        SwaggerFacet {

    @Inject
    private SwaggerConfiguration configuration;

    private static final String SWAGGER_DOCLET_VERSION_PROPERTY = "version.swagger-doclet";

    public static final Coordinate JAVADOC_PLUGIN_COORDINATE = CoordinateBuilder
            .create().setGroupId("org.apache.maven.plugins")
            .setArtifactId("maven-javadoc-plugin")
            .setVersion("2.10.3");

    @Override
    public boolean install() {
        addSwaggerDocletVersionProperty();
        addMavenPlugin();
        return isInstalled();
    }

    private void addMavenPlugin() {
        MavenPluginFacet pluginFacet = getFaceted().getFacet(MavenPluginFacet.class);

        MavenPluginBuilder javadocSwaggerPlugin = null;
        if (!pluginFacet.hasPlugin(JAVADOC_PLUGIN_COORDINATE)) {
            javadocSwaggerPlugin = MavenPluginBuilder
                    .create()
                    .setCoordinate(JAVADOC_PLUGIN_COORDINATE);
        } else {
            javadocSwaggerPlugin = MavenPluginBuilder.create(pluginFacet.getPlugin(JAVADOC_PLUGIN_COORDINATE));
        }

        javadocSwaggerPlugin.addExecution(
                ExecutionBuilder.create().addGoal("javadoc")
                .setPhase("generate-resources")
                .setId("generate-service-docs").
                setConfig(ConfigurationBuilder.create().
                        addConfigurationElement(getDocletConfig()).
                        addConfigurationElement(getDocletArtifact()).
                        addConfigurationElement(getOutputDir()).
                        addConfigurationElement(getAddidionalParam()))
        );

        if(pluginFacet.hasPlugin(JAVADOC_PLUGIN_COORDINATE)){
            System.out.println("UPDATE!");
            pluginFacet.updatePlugin(javadocSwaggerPlugin);
        } else{
            pluginFacet.addPlugin(javadocSwaggerPlugin);
        }
    }

    private void addSwaggerDocletVersionProperty() {
        MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
        Model pom = maven.getModel();
        Properties properties = pom.getProperties();
        if(!properties.contains(SWAGGER_DOCLET_VERSION_PROPERTY)){
             // TODO: Fetch the latest version
        properties.setProperty(SWAGGER_DOCLET_VERSION_PROPERTY, "1.1.1");
        maven.setModel(pom);
        }
       
    }
    @Override
    public boolean isInstalled() {
        MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
        return facet.hasPlugin(JAVADOC_PLUGIN_COORDINATE) && 
                hasSwaggerDocletExecution(facet.getPlugin(JAVADOC_PLUGIN_COORDINATE));
    }

    @Override
    public SwaggerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(SwaggerConfiguration configuration) {
        this.configuration = configuration;
    }

    private ConfigurationElement getDocletConfig() {
        return ConfigurationElementBuilder.create().setName("doclet")
                .setText("com.carma.swagger.doclet.ServiceDoclet");
    }

    /* <docletArtifact>
     <groupId>com.carma</groupId>
     <artifactId>swagger-doclet</artifactId>
     <version>1.0.2</version>
     </docletArtifact>
     */
    private ConfigurationElement getDocletArtifact() {

        return ConfigurationElementBuilder.create()
                .setName("docletArtifact")
                .addChild(ConfigurationElementBuilder.create()
                        .setName("groupId").setText("com.carma")).
                addChild(ConfigurationElementBuilder.create().
                        setName("artifactId").setText("swagger-doclet"))
                .addChild(ConfigurationElementBuilder.create().
                        setName("version").setText("${version.swagger-doclet}"));
    }

    /**
     * <reportOutputDirectory>src/main/webapp</reportOutputDirectory>
     */
    private ConfigurationElement getOutputDir() {
        return ConfigurationElementBuilder.create().setName("reportOutputDirectory")
                .setText(configuration.getOutputDir() == null ? "src/main/webapp" : configuration.getOutputDir());
    }

    private ConfigurationElement getAddidionalParam() {
        String projectName = getFaceted().getFacet(MetadataFacet.class).getProjectName();
        StringBuilder value = new StringBuilder(103);
        value.append("apiVersion 1").append("\n\t\t-docBasePath ").append(configuration.getDocBasePath() == null ? projectName + "/apidocs" : configuration.getApiBasePath())
                .append("\n\t\t-apiBasePath ").append(configuration.getApiBasePath() == null ? projectName + "/rest" : configuration.getApiBasePath())
                .append("\n\t\t-swaggerUiPath ${project.build.directory}/");
        return ConfigurationElementBuilder.create().setName("additionalparam")
                .setText(value.toString());
    }

    private boolean hasSwaggerDocletExecution(MavenPlugin plugin) {
        for (Execution exec : plugin.listExecutions()) {
            if(exec.getId().equals("generate-service-docs")){
                return true;
            }
        }
        return false;
    }
}
