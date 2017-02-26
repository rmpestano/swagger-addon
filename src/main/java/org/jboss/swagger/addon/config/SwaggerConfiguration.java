/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.swagger.addon.config;

/**
 *
 * @author pestano
 */
public interface SwaggerConfiguration {


  String getResourcesDir();

  SwaggerConfiguration setResourcesDir(String resourcesDir);
}
