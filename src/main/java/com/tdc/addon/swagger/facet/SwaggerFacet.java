package com.tdc.addon.swagger.facet;

import com.tdc.addon.swagger.config.SwaggerConfiguration;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.resource.Resource;

/**
 * The Swagger Facet
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public interface SwaggerFacet extends ProjectFacet {

    SwaggerConfiguration getConfiguration();

    void setConfiguration(SwaggerConfiguration configuration);

    boolean hasSwaggerUIResources();

	void generateSwaggerResources();
}
