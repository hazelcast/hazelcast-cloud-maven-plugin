package com.hazelcast.cloud.maven.client;

import java.io.File;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.cloud.maven.model.CustomerApiLogin;
import com.hazelcast.cloud.maven.model.CustomerTokenResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

public class HazelcastCloudClient {
    private String apiBaseUrl;

    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public HazelcastCloudClient(String apiBaseUrl, String apiKey, String apiSecret) {
        this.apiBaseUrl = apiBaseUrl;
        this.token = login(apiKey, apiSecret);
    }

    private String url(String uri) {
        return this.apiBaseUrl + uri;
    }

    private String login(String apiKey, String apiSecret) {
        var customerApiLogin = CustomerApiLogin.builder()
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .build();

        return restTemplate
            .postForObject(url("/customers/api/login"), customerApiLogin, CustomerTokenResponse.class)
            .getToken();
    }

    public void uploadCustomClasses(String clusterId, File file) {
        var headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        headers.add(AUTHORIZATION, "Bearer " + token);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("customClassesFile", new FileSystemResource(file));

        var requestEntity = new HttpEntity<>(body, headers);

        var pathParams = Map.of(
            "clusterId", clusterId
        );

        var response = restTemplate.postForEntity(
            url("/cluster/{clusterId}/custom_classes"),
            requestEntity,
            String.class,
            pathParams);
    }
}
