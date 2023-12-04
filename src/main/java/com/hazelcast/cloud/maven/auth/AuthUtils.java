package com.hazelcast.cloud.maven.auth;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@UtilityClass
public class AuthUtils {

    public static HttpHeaders headersWithAuth(String token) {
        var headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + token);
        return headers;
    }

}
