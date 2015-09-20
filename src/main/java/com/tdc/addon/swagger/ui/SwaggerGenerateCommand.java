/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tdc.addon.swagger.ui;

import com.tdc.addon.swagger.facet.SwaggerFacet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.javaee.cdi.CDIFacet;
import org.jboss.forge.addon.javaee.cdi.CDIFacet_1_0;
import org.jboss.forge.addon.javaee.cdi.CDIFacet_1_1;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.ui.annotation.Command;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.shrinkwrap.descriptor.api.beans11.Alternatives;
import org.jboss.shrinkwrap.descriptor.api.beans11.BeansDescriptor;

/**
 * Common CDI commands
 *
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class SwaggerGenerateCommand {

    @Inject
    private ProjectFactory projectFactory;
    
    @Command(value = "Swagger: Generate", enabled = RequiresSwaggerFacetPredicate.class, categories = {"Swagger"})
    public void execute(final UIContext context, final UIOutput output) {
        MavenFacet maven = getProject(context).getFacet(MavenFacet.class);
        maven.executeMaven(Arrays.asList("generate-resources"));
        output.success(output.out(),"Swagger generate command executed successfuly!");

    }

    private Project getProject(UIContext context) {
        return Projects.getSelectedProject(projectFactory, context);
    }
}
