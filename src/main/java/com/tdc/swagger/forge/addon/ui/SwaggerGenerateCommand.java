/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tdc.swagger.forge.addon.ui;

import com.tdc.swagger.forge.addon.facet.SwaggerFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;

/**
 * Swagger: Generate command
 *
 * @author <a href="rmpestano@gmail.com">Rafael Pestano</a>
 */
@FacetConstraint({SwaggerFacet.class})
public class SwaggerGenerateCommand extends AbstractProjectCommand {

    @Inject
    private ProjectFactory projectFactory;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("Swagger: Generate").
                category(Categories.create("Swagger")).
                description("Generate Swagger spec files for JAXRS endpoints of project "+getSelectedProject(context).getFacet(MetadataFacet.class).getProjectName().toUpperCase());
    }

    @Override
    public void initializeUI(UIBuilder uiBuilder) throws Exception {

    }

    @Override
    public Result execute(UIExecutionContext context) {
        getSelectedProject(context).getFacet(SwaggerFacet.class).generateSwaggerResources();
        return Results.success("Swagger generate command executed successfuly!");
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

}
