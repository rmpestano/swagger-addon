package org.jboss.swagger.addon.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
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
  private FacetFactory    facetFactory;

  @Inject
  SwaggerConfiguration    swaggerConfiguration;

  @Inject
  @WithAttributes(label = "API base path", description = "Base address of the REST API, defaults to 'contextPath/rest'")
  private UIInput<String> apiBasePath;

  @Inject
  @WithAttributes(label = "Doc base dir", description = "Api documentation artifacts (swagger spec json, html, js, css) base directory, defaults to 'src/main/webapp/'")
  private UIInput<String> docBaseDir;

  @Inject
  private ProjectFactory  projectFactory;

  @Override
  public UICommandMetadata getMetadata(UIContext context) {
    return Metadata.forCommand(getClass()).name("Swagger: Setup").category(Categories.create("Swagger")).description("Installs Swagger-ui artifacts and configures swagger-doclet for project " + getSelectedProject(context).getFacet(MetadataFacet.class).getProjectName().toUpperCase());
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
      swaggerConfiguration.setApiBasePath(apiBasePath.getValue()).setDocBaseDir(docBaseDir.getValue());
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
    Project selectedProject = Projects.getSelectedProject(projectFactory, builder.getUIContext());
    apiBasePath.setDefaultValue("/" + selectedProject.getRoot().getName() + "/rest");
    docBaseDir.setDefaultValue("src/main/webapp");
    builder.add(apiBasePath).add(docBaseDir);
  }

  private void copySwaggerUIResources(SwaggerFacet facet) {
    if (!facet.hasSwaggerUIResources()) {
      try {
        FileUtils.unzip(Thread.currentThread().getContextClassLoader().getResourceAsStream("/apidocs.zip"), facet.getFaceted().getRoot().reify(DirectoryResource.class).getOrCreateChildDirectory(facet.getConfiguration().getDocBaseDir() + "/apidocs".replaceAll("//", "/")).getFullyQualifiedName());
      } catch (Exception e) {
        LoggerFactory.getLogger(getClass().getName()).error("Could not unzip swagger ui resources", e);
      }
    }
  }
}
