package com.hazelcast.cloud.maven;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.var;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.reactivestreams.Publisher;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import com.hazelcast.cloud.maven.auth.ApiAuthenticator;
import com.hazelcast.cloud.maven.client.HazelcastCloudClient;

import static com.hazelcast.cloud.maven.cluster.ClusterIdExtractor.extractClusterId;
import static com.hazelcast.cloud.maven.validation.Errors.propertyMissingError;
import static org.codehaus.plexus.util.StringUtils.isEmpty;

@Mojo(name = "stream-logs", defaultPhase = LifecyclePhase.INITIALIZE)
@Setter
public class LogsHandler extends AbstractMojo {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Parameter(property = "apiBaseUrl", required = true)
    private String apiBaseUrl;

    @Parameter(property = "clusterName", required = true)
    private String clusterName;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "apiSecret", required = true)
    private String apiSecret;

    private Supplier<HazelcastCloudClient> hazelcastCloudClientSupplier =
        () -> new HazelcastCloudClient(apiBaseUrl, ApiAuthenticator.getToken(apiBaseUrl, apiKey, apiSecret));

    @Override
    public void execute() throws MojoExecutionException {
        validateParams();

        var clusterId = extractClusterId(clusterName);

        hazelcastCloudClientSupplier.get().getClusterLogs(clusterId)
            .flatMap(toStructuredLog())
            .filter(parsed -> !parsed.get("logger").equals("io.javalin.Javalin"))
            .filter(parsed -> !parsed.get("logger").startsWith("c.h.c.h.a"))
            .map(parsed -> String.join(" ",
                parsed.get("time"),
                parsed.get("logger"),
                parsed.get("level"),
                parsed.get("msg")
            )).toStream().forEach(System.out::println);
    }

    private Function<ServerSentEvent<String>, Publisher<? extends Map<String, String>>> toStructuredLog() {
        return sse -> {
            try {
                return Flux.just((Map<String, String>) objectMapper.readValue(sse.data(), Map.class));
            }
            catch (JsonProcessingException e) {
                return Flux.empty();
            }
        };
    }

    public void validateParams() throws MojoExecutionException {
        if (isEmpty(apiBaseUrl)) {
            propertyMissingError("apiBaseUrl");
        }
        if (isEmpty(clusterName)) {
            propertyMissingError("clusterName");
        }
        if (isEmpty(apiKey)) {
            propertyMissingError("apiKey");
        }
        if (isEmpty(apiSecret)) {
            propertyMissingError("apiSecret");
        }
    }
}
