package com.tdc.swagger.forge.addon.facet;

import com.tdc.swagger.forge.addon.config.SwaggerConfiguration;
import org.apache.maven.model.Model;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.maven.plugins.*;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFilter;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * The implementation of the {@link SwaggerFacet}
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class SwaggerFacetImpl extends AbstractFacet<Project> implements
        SwaggerFacet {

    public static final String SWAGGER_DOCLET_EXECUTION_ID = "generate-service-docs";

    private static final String SWAGGER_DOCLET_VERSION_PROPERTY = "version.swagger-doclet";
    
    private final List<String> allowedResourcesInApiDocs = Arrays.asList(new String[]{"css", "images","lib"});

    
    @Inject
    private SwaggerConfiguration configuration;


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
                                addConfigurationElement(ConfigurationElementBuilder.create().setName("useStandardDocletOptions")
                                        .setText("false")).
                                addConfigurationElement(getAddidionalParam()))
        );

        if (pluginFacet.hasPlugin(JAVADOC_PLUGIN_COORDINATE)) {
            pluginFacet.updatePlugin(javadocSwaggerPlugin);
        } else {
            pluginFacet.addPlugin(javadocSwaggerPlugin);
        }
    }

    private void addSwaggerDocletVersionProperty() {
        MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
        Model pom = maven.getModel();
        Properties properties = pom.getProperties();
        if (!properties.contains(SWAGGER_DOCLET_VERSION_PROPERTY)) {
            // TODO: Fetch the latest version
            properties.setProperty(SWAGGER_DOCLET_VERSION_PROPERTY, "1.1.1");
            maven.setModel(pom);
        }

    }

    @Override
    public boolean isInstalled() {
        MavenPluginFacet facet = getFaceted().getFacet(MavenPluginFacet.class);
        return facet.hasPlugin(JAVADOC_PLUGIN_COORDINATE)
                && hasSwaggerDocletExecution(facet.getPlugin(JAVADOC_PLUGIN_COORDINATE));
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

    private ConfigurationElement getAddidionalParam() {
        String projectName = getFaceted().getFacet(MetadataFacet.class).getProjectName();
        StringBuilder value = new StringBuilder(103);
        value.append("-apiVersion 1")
                .append("\n\t\t-docBasePath ").append(configDocBasePath())
                .append("\n\t\t-apiBasePath ").append(configuration.getApiBasePath() == null ? projectName + "/rest" : configuration.getApiBasePath())
                /**
                 * we will patch swagger ui inside 
                 * forge addon so no need to use the one bundled with swagger-doclet
                 */
                .append("\n\t\t-swaggerUiPath ${project.build.directory}/");

        return ConfigurationElementBuilder.create().setName("additionalparam")
                .setText(value.toString() + " \n\t\t");
    }

    /**
     * configure docbase path based on outputDir.
     * Ex: if outputdir is 'src/main/webapp/rest'
     * then docbasePath will be: 'contextPath/rest/apidocs'
     *
     * Note that it will use 'webapp' folder as root path.
     *
     * FIXME: introduce a new param in setup command to configure docBasePath (and outputDir) and remove this method
     *
     * @return
     */
    private String configDocBasePath() {
        String projectName = getFaceted().getFacet(MetadataFacet.class).getProjectName();
        StringBuilder docBasePath = new StringBuilder("/"+projectName);
        if(getOutputDir() == null || !getOutputDir().getText().contains("webapp")){
            docBasePath.append('/').append(getOutputDir().getText()).append("/apidocs");
        } else{
            String outputDirText = getOutputDir().getText();
            //remove webapp folder
            String docPathSulfix = outputDirText.substring(outputDirText.indexOf("webapp")+6);
            docBasePath.append('/').append(docPathSulfix).append("apidocs");
        }

        return docBasePath.toString().replaceAll("//","/");
    }

    private boolean hasSwaggerDocletExecution(MavenPlugin plugin) {
        for (Execution exec : plugin.listExecutions()) {
            if (exec.getId().equals(SWAGGER_DOCLET_EXECUTION_ID)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * directory where swagger (json) spec files will be generated
     * Note that it must be the same dir where swagger ui artifacts reside
     * @see SwaggerFacetImpl#copySwaggerUIResources()
     * @return this
     */
    private ConfigurationElement getOutputDir() {
                return ConfigurationElementBuilder.create().setName("reportOutputDirectory")
                .setText(configuration.getDocBaseDir());
    }


    public boolean hasSwaggerUIResources() {
        Resource<?> apiDocs = getFaceted().getRoot().getChild(configuration.getDocBaseDir()+"/apidocs");
        return apiDocs.exists() && apiDocs.getChild("index.html").exists();

    }

    @Override
    public void generateSwaggerResources() {
    	MavenFacet maven = getFaceted().getFacet(MavenFacet.class);
        maven.executeMaven(Arrays.asList("generate-resources"));
    	deleteUnusedSwaggerResources();
    }

	public void deleteUnusedSwaggerResources() {
		Project project = getFaceted();
        Resource<?> apiDocs = project.getRoot().reify(DirectoryResource.class).getChild(configuration.getDocBaseDir() + "/apidocs");
        ResourceFilter unusedResources = new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource || resource.getName().endsWith(".war");
            }
        };
        for (Resource<?> resource : apiDocs.listResources(unusedResources)){
            if(!allowedResourcesInApiDocs.contains(resource.getName()) || resource.getName().endsWith(".war")){
                resource.delete(true);
            }
        }
		
	}
	 


}
