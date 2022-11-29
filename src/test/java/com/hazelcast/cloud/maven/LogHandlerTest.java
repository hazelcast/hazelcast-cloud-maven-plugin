package com.hazelcast.cloud.maven;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import lombok.var;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import com.hazelcast.cloud.maven.client.HazelcastCloudClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class LogHandlerTest {

    @ParameterizedTest
    @MethodSource
    public void should_fail_given_invalid_params(
        String apiBaseUrl,
        String clusterName,
        String apiKey,
        String apiSecret
    ) {
        // given
        var deployHandler = new LogsHandler();
        deployHandler.setApiBaseUrl(apiBaseUrl);
        deployHandler.setClusterName(clusterName);
        deployHandler.setApiKey(apiKey);
        deployHandler.setApiSecret(apiSecret);

        // when
        var exception = assertThrows(MojoExecutionException.class, deployHandler::execute);

        // then
        then(exception.getMessage())
            .startsWith("Configuration property '")
            .endsWith("' is missing or empty");
    }

    private static Stream<Arguments> should_fail_given_invalid_params() {
        return Stream.of(
            Arguments.of(null, "pr-a1b2c3d4", "api-key", "api-secret"),
            Arguments.of("https://api.viridian.hazelcast.com", null, "api-key", "api-key"),
            Arguments.of("https://api.viridian.hazelcast.com", "pr-a1b2c3d4", null, "api-secret"),
            Arguments.of("https://api.viridian.hazelcast.com", "pr-a1b2c3d4", "api-key", null)
        );
    }

    @Test
    public void should_fail_given_invalid_cluster_name() {
        // given
        var logsHandler = logsHandler();
        logsHandler.setClusterName("a1b2c3d4");

        // when
        var exception = assertThrows(MojoExecutionException.class, logsHandler::execute);

        // then
        then(exception.getMessage()).isEqualTo("Invalid clusterName (example: pr-a1b2c3d4)");
    }

    @Test
    public void should_stream_cluster_logs() throws MojoExecutionException {
        // given
        var handler = logsHandler();
        var hazelcastClient = mock(HazelcastCloudClient.class);
        handler.setHazelcastCloudClientSupplier(() -> hazelcastClient);

        given(hazelcastClient.getClusterLogs("a1b2c3d4")).willReturn(
            Flux.just(
                ServerSentEvent.<String>builder().data("{\"time\":\"2022-09-01T15:50:08Z\","
                        + "\"logger\":\"com.hazelcast.instance.impl.Node\",\"level\":\"INFO\",\"msg\":\"[10.0.39"
                        + ".78]:5701 [pr-307] [5.1.1] Using Discovery SPI \",\"clusterId\":\"307\","
                        + "\"customerId\":\"105\"}")
                    .build(),
                ServerSentEvent.<String>builder().data("{\"time\":\"2022-09-01T15:50:08Z\",\"logger\":\"com"
                        + ".hazelcast.core.LifecycleService\",\"level\":\"INFO\",\"msg\":\"[10.0.39.78]:5701 "
                        + "[pr-307] [5.1.1] [10.0.39.78]:5701 is STARTING \",\"clusterId\":\"307\","
                        + "\"customerId\":\"105\"}")
                    .build())
        );

        // when
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream sysout = System.out;
        System.setOut(new PrintStream(out));
        try {
            handler.execute();
        }
        finally {
            System.setOut(sysout);
        }

        // then
        assertThat(out.toString())
            .contains(
                "2022-09-01T15:50:08Z com.hazelcast.instance.impl.Node INFO [10.0.39.78]:5701 [pr-307] [5.1.1] "
                    + "Using Discovery SPI")
            .contains(
                "2022-09-01T15:50:08Z com.hazelcast.core.LifecycleService INFO [10.0.39.78]:5701 [pr-307] [5.1"
                    + ".1] [10.0.39.78]:5701 is STARTING");
    }

    private LogsHandler logsHandler() {
        LogsHandler handler = new LogsHandler();
        handler.setApiBaseUrl("https://localhost");
        handler.setClusterName("pr-a1b2c3d4");
        handler.setApiKey("api-key");
        handler.setApiSecret("api-key");

        return handler;
    }

}
