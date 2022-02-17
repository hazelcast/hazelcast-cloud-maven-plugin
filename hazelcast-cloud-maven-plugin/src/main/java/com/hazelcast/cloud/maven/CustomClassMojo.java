package com.hazelcast.cloud.maven;

import lombok.Data;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.hazelcast.cloud.maven.client.HazelcastCloudClient;

import static org.codehaus.plexus.util.StringUtils.isEmpty;

@Mojo(name = "custom-class-upload", defaultPhase = LifecyclePhase.DEPLOY)
@Data
public class CustomClassMojo extends AbstractMojo {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateParams();

        var hazelcastCloudClient = new HazelcastCloudClient(apiBaseUrl, apiKey, apiSecret);
        var jar = project.getArtifact().getFile();

        this.getLog().info(String.format(
            "Artifact with custom classes %s is being uploaded to the Hazelcast cluster '%s'", jar, clusterId));

        hazelcastCloudClient.uploadCustomClasses(clusterId, jar);
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
