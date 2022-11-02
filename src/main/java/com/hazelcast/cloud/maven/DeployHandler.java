package com.hazelcast.cloud.maven;

import java.util.function.Supplier;

import lombok.Setter;
import lombok.var;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.retry.support.RetryTemplateBuilder;

import com.hazelcast.cloud.maven.auth.ApiAuthenticator;
import com.hazelcast.cloud.maven.client.HazelcastCloudClient;
import com.hazelcast.cloud.maven.cluster.ClusterIdExtractor;
import com.hazelcast.cloud.maven.exception.ClusterFailureException;

import static com.hazelcast.cloud.maven.validation.Errors.propertyMissingError;
import static java.lang.System.currentTimeMillis;
import static org.codehaus.plexus.util.StringUtils.isEmpty;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
@Setter
public class DeployHandler extends AbstractMojo {

    @Parameter(property = "apiBaseUrl", defaultValue = "https://api.viridian.hazelcast.com")
    private String apiBaseUrl;

    @Parameter(property = "clusterName", required = true)
    private String clusterName;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "apiSecret", required = true)
    private String apiSecret;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private Supplier<HazelcastCloudClient> hazelcastCloudClientSupplier =
        () -> new HazelcastCloudClient(apiBaseUrl, ApiAuthenticator.getToken(apiBaseUrl, apiKey, apiSecret));

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        long startTime = currentTimeMillis();

        validateParams();
        var clusterId = ClusterIdExtractor.extractClusterId(clusterName);

        var hazelcastCloudClient = hazelcastCloudClientSupplier.get();
        var jar = project.getArtifact().getFile();

        getLog().info(String.format(
            "Artifact with custom classes %s is being uploaded to the Hazelcast cluster '%s'", jar, clusterName));

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
        if (isEmpty(clusterName)) {
            propertyMissingError("clusterName");
        }
        if (isEmpty(apiKey)) {
            propertyMissingError("apiKey");
        }
        if (isEmpty(apiSecret)) {
            propertyMissingError("apiSecret");
        }
        if (project.getArtifact() == null || project.getArtifact().getFile() == null) {
            throw new MojoExecutionException(
                "Project artifact (jar) is not packaged. Execute 'package' goal prior to 'deploy'.");
        }
    }

}
