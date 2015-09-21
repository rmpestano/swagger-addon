/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdc.addon.swagger.config;

import javax.inject.Singleton;

@Singleton
public class SwaggerConfigurationImpl implements SwaggerConfiguration {

    private String docBaseDir;
    private String apiBasePath;

    

    @Override
    public String getDocBaseDir() {
        return docBaseDir;
    }

    @Override
    public SwaggerConfiguration setDocBaseDir(String docBasePath) {
        this.docBaseDir = docBasePath;
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
