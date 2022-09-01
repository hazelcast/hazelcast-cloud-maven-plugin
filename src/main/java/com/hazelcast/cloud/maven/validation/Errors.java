package com.hazelcast.cloud.maven.validation;

import lombok.experimental.UtilityClass;
import org.apache.maven.plugin.MojoExecutionException;

@UtilityClass
public class Errors {

    public static void propertyMissingError(String propertyName) throws MojoExecutionException {
        throw new MojoExecutionException(
            String.format("Configuration property '%s' is missing or empty", propertyName));
    }

}
