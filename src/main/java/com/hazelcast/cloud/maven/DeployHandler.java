package com.hazelcast.cloud.maven;

import java.util.function.Supplier;

import lombok.Data;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.retry.support.RetryTemplateBuilder;

import com.hazelcast.cloud.maven.client.HazelcastCloudClient;
import com.hazelcast.cloud.maven.exception.ClusterFailureException;

import static java.lang.System.currentTimeMillis;
import static org.codehaus.plexus.util.StringUtils.isEmpty;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
@Data
public class DeployHandler extends AbstractMojo {

    @Parameter(property = "apiBaseUrl", required = true)
    private String apiBaseUrl;

    @Parameter(property = "clusterId", required = true)
    private String clusterId;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "apiSecret", required = true)
    private String apiSecret;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private Supplier<HazelcastCloudClient> hazelcastCloudClientSupplier =
        () -> new HazelcastCloudClient(apiBaseUrl, apiKey, apiSecret);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var startTime = currentTimeMillis();

        validateParams();

        var hazelcastCloudClient = hazelcastCloudClientSupplier.get();
        var jar = project.getArtifact().getFile();

        getLog().info(String.format(
            "Artifact with custom classes %s is being uploaded to the Hazelcast cluster '%s'", jar, clusterId));

        hazelcastCloudClient.uploadCustomClasses(clusterId, jar);

        try {
            new RetryTemplateBuilder()
                .withinMillis(120_000)
                .fixedBackoff(1000)
                .retryOn(IllegalStateException.class)
                .build()
                .execute(retryContext -> {
                    var secs = (currentTimeMillis() - startTime) / 1000;
                    getLog().info(secs + "s");

                    var state = hazelcastCloudClient.getClusterStatus(clusterId).state;
                    switch (state) {
                        case "RUNNING":
                            return state;
                        case "FAILED":
                            throw new ClusterFailureException(String.format(
                                "Something is wrong with cluster, state: %s", state));
                        default:
                            throw new IllegalStateException(String.format(
                                "Something went wrong with uploading %s, state: %s", jar.getName(), state));
                    }
                });

            getLog().info(String.format(
                "Artifact with custom classes %s was uploaded and is ready to be used", jar.getName()));

            var totalTimeSec = (currentTimeMillis() - startTime) / 1000f;
            getLog().info(String.format("Artifact upload total time: %.3f s", totalTimeSec));
        }
        catch (IllegalStateException | ClusterFailureException exception) {
            getLog().error(exception.getMessage());
            throw new MojoFailureException(exception.getMessage());
        }
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

    private void propertyMissingError(String propertyName) throws MojoExecutionException {
        throw new MojoExecutionException(
            String.format("Configuration property '%s' is missing or empty", propertyName));
    }
}
