package com.hazelcast.cloud.maven.cluster;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.hazelcast.cloud.maven.cluster.ClusterIdExtractor.extractClusterId;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClusterIdExtractorTest {
    @ParameterizedTest
    @ValueSource(strings = {"pr-1a2b3c4d", "pr-123"})
    public void shouldExtractClusterId(String clusterName) throws MojoExecutionException {
        extractClusterId(clusterName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1a2b3c4d", "1234", "Other name"})
    public void shouldFailExtractingClusterId(String clusterName) {
        assertThrows(
            MojoExecutionException.class,
            () -> extractClusterId(clusterName),
            "Invalid clusterName (example: pr-a1b2c3d4)"
        );
    }
}
