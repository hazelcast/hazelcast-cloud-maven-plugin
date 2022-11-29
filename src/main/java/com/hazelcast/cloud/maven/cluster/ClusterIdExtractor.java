package com.hazelcast.cloud.maven.cluster;

import org.apache.maven.plugin.MojoExecutionException;

public final class ClusterIdExtractor {

    public static String extractClusterId(String clusterName) throws MojoExecutionException {
        if (!clusterName.matches("[a-z]{2}-[0-9a-z]+")) {
            throw new MojoExecutionException("Invalid clusterName (example: pr-a1b2c3d4)");
        }

        return clusterName.split("-")[1];
    }
}
