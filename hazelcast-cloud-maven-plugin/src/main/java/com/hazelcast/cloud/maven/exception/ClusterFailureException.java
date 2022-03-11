package com.hazelcast.cloud.maven.exception;

public class ClusterFailureException extends RuntimeException {
    public ClusterFailureException(String message) {
        super(message);
    }
}
