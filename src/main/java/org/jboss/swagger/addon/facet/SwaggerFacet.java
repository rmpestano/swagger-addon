package org.jboss.swagger.addon.facet;

import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.swagger.addon.config.SwaggerConfiguration;

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
