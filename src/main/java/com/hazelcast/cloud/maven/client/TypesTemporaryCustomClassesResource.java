package com.hazelcast.cloud.maven.client;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypesTemporaryCustomClassesResource {

    private String id;

    private String name;

    private Set<String> classes;

}