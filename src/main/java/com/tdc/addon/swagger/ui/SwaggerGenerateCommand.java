/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tdc.addon.swagger.ui;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFilter;
import org.jboss.forge.addon.ui.annotation.Command;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.output.UIOutput;

import com.tdc.addon.swagger.facet.SwaggerFacet;

/**
 * Common CDI commands
 *
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class SwaggerGenerateCommand {

    @Inject
    private ProjectFactory projectFactory;
    
    @Command(value = "Swagger: Generate", enabled = RequiresSwaggerFacetPredicate.class, categories = {"Swagger"}, help="Generate Swagger spec files based selected project on JaxRS endpoints" )
    public void execute(final UIContext context, final UIOutput output) {
        getProject(context).getFacet(SwaggerFacet.class).generateSwaggerResources();
        output.success(output.out(),"Swagger generate command executed successfuly!");
    }


    private Project getProject(UIContext context) {
        return Projects.getSelectedProject(projectFactory, context);
    }
}
