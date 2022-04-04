package com.hazelcast.cloud.maven.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerApiLogin {
    private String apiKey;

    private String apiSecret;
}
