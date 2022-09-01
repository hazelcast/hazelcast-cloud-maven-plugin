package com.hazelcast.cloud.maven.cluster;

import lombok.experimental.UtilityClass;
import org.apache.maven.plugin.MojoExecutionException;

@UtilityClass
public class ClusterIdExtractor {

    public String extractClusterId(String clusterName) throws MojoExecutionException {
        if (!clusterName.matches("[a-z]{2}-\\d+")) {
            throw new MojoExecutionException("Invalid clusterName (example: de-1234)");
        }

        return clusterName.split("-")[1];
    }

}
