package org.jboss.swagger.addon.util;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.swagger.addon.facet.SwaggerFacetImpl;

import javax.inject.Inject;
import java.util.Comparator;

/**
 * Created by rmpestano on 25/02/17.
 */
public class DependencyUtil {

    @Inject
    private DependencyResolver resolver;

     public Coordinate getLatestAnalyzerVersion() {
         return resolver.resolveVersions(DependencyQueryBuilder.create(SwaggerFacetImpl.ANALYZER_PLUGIN_COORDINATE))
                 .stream().reduce((a, b) -> b).orElse(null);
     }

}
