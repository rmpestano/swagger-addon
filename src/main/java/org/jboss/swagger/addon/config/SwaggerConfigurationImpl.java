/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.swagger.addon.config;

import org.jboss.forge.addon.maven.projects.MavenPluginFacet;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SwaggerConfigurationImpl implements SwaggerConfiguration {

  private String resourcesDir;


  @Override
  public SwaggerConfiguration setResourcesDir(String resourcesDir) {
    this.resourcesDir = resourcesDir;
    return this;
  }

  public String getResourcesDir() {
    return resourcesDir;
  }


}
