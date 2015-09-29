package com.tdc.swagger.forge.addon.facet;

import com.tdc.swagger.forge.addon.config.SwaggerConfiguration;

import org.jboss.forge.addon.projects.ProjectFacet;

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
