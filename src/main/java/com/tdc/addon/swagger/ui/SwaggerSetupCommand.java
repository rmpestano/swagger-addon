package com.tdc.addon.swagger.ui;

import com.tdc.addon.swagger.config.SwaggerConfiguration;
import com.tdc.addon.swagger.facet.SwaggerFacet;
import static com.tdc.addon.swagger.facet.SwaggerFacetImpl.JAVADOC_PLUGIN_COORDINATE;
import java.util.Iterator;
import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.maven.plugins.ConfigurationElement;
import org.jboss.forge.addon.maven.plugins.Execution;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Swagger: Setup command
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class SwaggerSetupCommand extends AbstractProjectCommand {

    @Inject
    private FacetFactory facetFactory;
    
        
    @Inject
    SwaggerConfiguration swaggerConfiguration;

    @Inject
    @WithAttributes(label = "Context path", description = "Web application context path")
    private UIInput<String> contextPath;
    
    @Inject
    @WithAttributes(label = "Output dir", description = "Swagger artifacts output dir", defaultValue = "src/main/webapp")
    private UIInput<String> outputDir;
    
    @Inject
    @WithAttributes(label = "Doc base path", description = "Api documentation directory", defaultValue = "src/main/webapp/apidocs")
    private UIInput<String> docBasePath;
    
    @Inject
    @WithAttributes(label = "API base path", description = "Api base path")
    private UIInput<String> apiBasePath;

   
    @Inject
    private ProjectFactory projectFactory;
    

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Swagger: Setup").category(Categories.create("Swagger"));
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);
        
        boolean execute = true;
        if(project.hasFacet(SwaggerFacet.class) && project.getFacet(SwaggerFacet.class).isInstalled()){
            execute = context.getPrompt().promptBoolean("Swagger plugin is already installed, override it?");
        }
        
        if(execute){
            swaggerConfiguration.setContextPath(contextPath.getValue()).
                setApiBasePath(apiBasePath.getValue()).
                setDocBasePath(docBasePath.getValue()).
                setOutputDir(outputDir.getValue());
        SwaggerFacet facet = facetFactory.create(project, SwaggerFacet.class);
        facet.setConfiguration(swaggerConfiguration);
        facetFactory.install(project, facet);
        return Results.success("Swagger setup completed successfully!");
        } else{
            return Results.success();
        }
        
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project seleProject = Projects.getSelectedProject(projectFactory, builder.getUIContext());
        apiBasePath.setDefaultValue(seleProject.getRoot().getName() + "/rest");
        contextPath.setDefaultValue(seleProject.getRoot().getName());
        builder.add(contextPath).add(outputDir).add(apiBasePath).add(docBasePath);
    }
}
