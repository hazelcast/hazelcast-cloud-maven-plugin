package com.hazelcast.cloud.maven.client;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BatchClusterCustomClassesRequest {

    private Add add = new Add();

    private Delete delete = new Delete();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Add {
        private Set<String> temporaryCustomClassesIds = new LinkedHashSet<>();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Delete {
        private Set<Integer> ids = new LinkedHashSet<>();
    }

}
