package com.tdc.addon.swagger.facet;

import com.tdc.addon.swagger.config.SwaggerConfiguration;
import org.jboss.forge.addon.projects.ProjectFacet;

/**
 * The Wildfly-Swarm Facet
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public interface SwaggerFacet extends ProjectFacet {

    SwaggerConfiguration getConfiguration();

    void setConfiguration(SwaggerConfiguration configuration);
}
