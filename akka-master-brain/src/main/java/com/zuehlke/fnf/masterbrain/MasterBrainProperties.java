package com.zuehlke.fnf.masterbrain;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ConfigurationProperties(prefix = "masterbrain")
// see: /resources/application.yml
public class MasterBrainProperties {

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

}
