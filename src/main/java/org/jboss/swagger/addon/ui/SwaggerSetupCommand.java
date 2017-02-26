package org.jboss.swagger.addon.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
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
import org.jboss.swagger.addon.config.SwaggerConfiguration;
import org.jboss.swagger.addon.facet.SwaggerFacet;
import org.jboss.swagger.addon.util.FileUtils;
import org.slf4j.LoggerFactory;

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
    @WithAttributes(label = "Resources dir", description = "Directory where swagger json spec files and swagger-ui resources will be generated. Defaults to /apidocs")
    private UIInput<String> resourcesDir;


    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private MavenFacet mavenFacet;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Swagger: Setup")
                .category(Categories.create("Swagger"))
                .description(resolveDescription(context));
    }

    private String resolveDescription(UIContext context) {
        return "Installs Swagger-ui artifacts and configures jaxrs-analyzer maven plugin"
                + (getSelectedProject(context) != null ? "for project "
                + getSelectedProject(context).getFacet(MetadataFacet.class).getProjectName().toUpperCase() : "");
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
        if (project.hasFacet(SwaggerFacet.class) && project.getFacet(SwaggerFacet.class).isInstalled()) {
            execute = context.getPrompt().promptBoolean("Swagger plugin is already installed, override it?");
        }

        if (execute) {
            swaggerConfiguration.setResourcesDir(resourcesDir.getValue());
            SwaggerFacet facet = facetFactory.create(project, SwaggerFacet.class);
            facet.setConfiguration(swaggerConfiguration);
            facetFactory.install(project, facet);
            copySwaggerUIResources(facet);
            return Results.success("Swagger setup completed successfully!");
        } else {
            return Results.success();
        }

    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        resourcesDir.setDefaultValue("/apidocs");
        builder.add(resourcesDir);
    }

    private void copySwaggerUIResources(SwaggerFacet facet) {
        if (!facet.hasSwaggerUIResources()) {
            try {
                FileUtils.unzip(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("/apidocs.zip"), facet.getFaceted().getRoot()
                        .reify(DirectoryResource.class)
                        .getOrCreateChildDirectory("src")
                        .getOrCreateChildDirectory("main")
                        .getOrCreateChildDirectory("webapp")
                        .getOrCreateChildDirectory(facet.getConfiguration()
                                .getResourcesDir().replaceAll("//", "/")).getFullyQualifiedName());
            } catch (Exception e) {
                LoggerFactory.getLogger(getClass().getName()).error("Could not unzip swagger ui resources", e);
            }
        }
    }
}
