/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdc.swagger.forge.addon.config;

/**
 *
 * @author pestano
 */
public interface SwaggerConfiguration {


    String getDocBaseDir();

    String getApiBasePath();

    SwaggerConfiguration setDocBaseDir(String outputDir);

    SwaggerConfiguration setApiBasePath(String outputDir);

}
