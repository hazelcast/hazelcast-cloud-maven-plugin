package com.hazelcast.cloud.maven.model;

import java.util.Map;

public class Cluster {
    public String desiredState;
    public String state;

    public String getDesiredState() {
        return desiredState;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Cluster{" +
          "desiredState='" + desiredState + '\'' +
          ", state='" + state + '\'' +
          '}';
    }
}
