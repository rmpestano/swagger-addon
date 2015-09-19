/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdc.addon.swagger.config;

/**
 *
 * @author pestano
 */
public interface SwaggerConfiguration {

    String getContextPath();

    String getOutputDir();

    String getDocBasePath();

    String getApiBasePath();

    SwaggerConfiguration setContextPath(String contextPath);

    SwaggerConfiguration setOutputDir(String outputDir);

    SwaggerConfiguration setDocBasePath(String outputDir);

    SwaggerConfiguration setApiBasePath(String outputDir);

}
