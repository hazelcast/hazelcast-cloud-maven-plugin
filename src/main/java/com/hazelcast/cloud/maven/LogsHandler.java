package com.hazelcast.cloud.maven;

import java.io.IOException;

import lombok.var;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.hazelcast.cloud.maven.client.HazelcastCloudClient;

@Mojo(name = "logs", defaultPhase = LifecyclePhase.DEPLOY)
public class LogsHandler extends AbstractMojo {

    @Parameter(property = "apiBaseUrl", required = true)
    private String apiBaseUrl;

    @Parameter(property = "clusterId", required = true)
    private String clusterId;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "apiSecret", required = true)
    private String apiSecret;
    @Override
    public void execute() {
        var hazelcastCloudClient = new HazelcastCloudClient(apiBaseUrl, apiKey, apiSecret);

        try {
            hazelcastCloudClient.getClusterLogs(clusterId).forEach(System.out::println);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
