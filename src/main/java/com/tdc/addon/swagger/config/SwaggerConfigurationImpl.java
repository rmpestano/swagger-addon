/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdc.addon.swagger.config;

import javax.inject.Singleton;

@Singleton
public class SwaggerConfigurationImpl implements SwaggerConfiguration {

    private String contextPath;
    private String outputDir;
    private String docBasePath;
    private String apiBasePath;

    
    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getOutputDir() {
        return outputDir;
    }

    @Override
    public SwaggerConfiguration setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * app dir (under webapp) where swagger artifacts will be generated
     *
     * @param outputDir
     * @return
     */
    @Override
    public SwaggerConfiguration setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    @Override
    public String getDocBasePath() {
        return docBasePath;
    }

    @Override
    public SwaggerConfiguration setDocBasePath(String docBasePath) {
        this.docBasePath = docBasePath;
        return this;
    }

    @Override
    public String getApiBasePath() {
        return apiBasePath;
    }

    @Override
    public SwaggerConfiguration setApiBasePath(String apiBasePath) {
        this.apiBasePath = apiBasePath;
        return this;
    }

}
