package com.hazelcast.cloud.maven;

import java.io.File;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.hazelcast.cloud.maven.client.HazelcastCloudClient;
import com.hazelcast.cloud.maven.model.Cluster;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeployHandlerTest {
    @ParameterizedTest
    @MethodSource
    public void should_fail_given_invalid_params(
        String apiBaseUrl,
        String clusterId,
        String apiKey,
        String apiSecret
    ) {
        // given
        var deployHandler = new DeployHandler();
        deployHandler.setApiBaseUrl(apiBaseUrl);
        deployHandler.setClusterId(clusterId);
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
            Arguments.of(null, "a1b2c3d4", "api-key", "api-secret"),
            Arguments.of("https://api.cloud.hazelcast.com", null, "api-key", "api-key"),
            Arguments.of("https://api.cloud.hazelcast.com", "a1b2c3d4", null, "api-secret"),
            Arguments.of("https://api.cloud.hazelcast.com", "a1b2c3d4", "api-key", null)
        );
    }

    @Test
    public void should_fail_if_artifact_absent() {
        // given
        var deployHandler = deployHandler();
        var mavenProject = mock(MavenProject.class);
        deployHandler.setProject(mavenProject);

        given(mavenProject.getArtifact()).willReturn(null);

        // when
        var exception = assertThrows(MojoExecutionException.class, deployHandler::execute);

        // then
        then(exception.getMessage())
            .isEqualTo("Project artifact (jar) is not packaged. Execute 'package' goal prior to 'deploy'.");
    }

    @Test
    public void should_upload_custom_classes_jar() throws MojoExecutionException, MojoFailureException {
        // given
        var deployHandler = deployHandler();
        var mavenProject = mockMavenProject();
        deployHandler.setProject(mavenProject);
        var hazelcastClient = mock(HazelcastCloudClient.class);
        deployHandler.setHazelcastCloudClientSupplier(() -> hazelcastClient);

        given(hazelcastClient.getClusterStatus("a1b2c3d4")).willReturn(
            Cluster.builder().state("PENDING").build(),
            Cluster.builder().state("RUNNING").build()
        );

        // when
        deployHandler.execute();

        // then
        verify(hazelcastClient).uploadCustomClasses("a1b2c3d4", mavenProject.getArtifact().getFile());
    }

    @Test
    public void should_fail_if_cluster_failed() {
        // given
        var deployHandler = deployHandler();
        var hazelcastClient = mock(HazelcastCloudClient.class);
        deployHandler.setProject(mockMavenProject());
        deployHandler.setHazelcastCloudClientSupplier(() -> hazelcastClient);

        given(hazelcastClient.getClusterStatus("a1b2c3d4")).willReturn(Cluster.builder().state("FAILED").build());

        // when
        var exception = assertThrows(MojoFailureException.class, deployHandler::execute);

        // then
        then(exception.getMessage()).isEqualTo("Something is wrong with cluster, state: FAILED");
    }

    private DeployHandler deployHandler() {
        var deployHandler = new DeployHandler();
        deployHandler.setApiBaseUrl("https://localhost");
        deployHandler.setClusterId("a1b2c3d4");
        deployHandler.setApiKey("api-key");
        deployHandler.setApiSecret("api-key");

        return deployHandler;
    }

    private MavenProject mockMavenProject() {
        var mavenProject = mock(MavenProject.class);
        var artifact = mock(Artifact.class);
        given(mavenProject.getArtifact()).willReturn(artifact);
        given(artifact.getFile()).willReturn(new File("custom-classes.jar"));

        return mavenProject;
    }
}
