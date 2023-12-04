package com.hazelcast.cloud.maven;

import java.util.function.Supplier;

import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.http.codec.ServerSentEvent;

import com.hazelcast.cloud.maven.auth.ApiAuthenticator;
import com.hazelcast.cloud.maven.client.HazelcastCloudClient;

import static com.hazelcast.cloud.maven.validation.Errors.propertyMissingError;
import static org.codehaus.plexus.util.StringUtils.isEmpty;

@Mojo(name = "stream-logs", defaultPhase = LifecyclePhase.INITIALIZE)
@Setter
public class LogsHandler extends AbstractMojo {

    @Parameter(property = "apiBaseUrl", required = true)
    private String apiBaseUrl;

    @Parameter(property = "clusterId", required = true)
    private String clusterId;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "apiSecret", required = true)
    private String apiSecret;

    private Supplier<HazelcastCloudClient> hazelcastCloudClientSupplier =
        () -> new HazelcastCloudClient(apiBaseUrl, ApiAuthenticator.getToken(apiBaseUrl, apiKey, apiSecret));

    @Override
    public void execute() throws MojoExecutionException {
        validateParams();

        hazelcastCloudClientSupplier.get().getClusterLogs(clusterId)
            .mapNotNull(ServerSentEvent::data)
            .toStream()
            .forEach(System.out::println);
    }

    public void validateParams() throws MojoExecutionException {
        if (isEmpty(apiBaseUrl)) {
            propertyMissingError("apiBaseUrl");
        }
        if (isEmpty(clusterId)) {
            propertyMissingError("clusterId");
        }
        if (isEmpty(apiKey)) {
            propertyMissingError("apiKey");
        }
        if (isEmpty(apiSecret)) {
            propertyMissingError("apiSecret");
        }
    }
}
