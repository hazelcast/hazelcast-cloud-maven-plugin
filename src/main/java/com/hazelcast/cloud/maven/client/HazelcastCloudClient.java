package com.hazelcast.cloud.maven.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.cloud.maven.model.Cluster;
import com.hazelcast.cloud.maven.model.CustomerApiLogin;
import com.hazelcast.cloud.maven.model.CustomerTokenResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

public class HazelcastCloudClient {
    private final String apiBaseUrl;

    private final String token;

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

    public Cluster getClusterStatus(String clusterId) {
        return restTemplate.exchange(
            url("/cluster/{clusterId}"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithAuth()),
            Cluster.class,
            clusterId).getBody();
    }

    public void uploadCustomClasses(String clusterId, File file) {
        var headers = headersWithAuth();
        headers.setContentType(MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("customClassesFile", new FileSystemResource(file));

        var pathParams = new HashMap<String, String>();
        pathParams.put("clusterId", clusterId);

        restTemplate.postForEntity(
            url("/cluster/{clusterId}/custom_classes"),
            new HttpEntity<>(body, headers),
            String.class,
            pathParams);
    }

    public Stream<String> getClusterLogs(String clusterId) throws IOException, InterruptedException {
        var uri = URI.create(String.format("%s/cluster/%s/log-stream", apiBaseUrl, clusterId));
        var httpClient = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(uri)
            .header(AUTHORIZATION, "Bearer " + token)
            .GET()
            .build();

        ObjectMapper objectMapper = new ObjectMapper();

        return httpClient.send(req, HttpResponse.BodyHandlers.ofLines())
            .body()
            .map(line -> line.replaceFirst("data:", ""))
            .filter(s -> !s.trim().isEmpty())
            .flatMap(line -> {
                try {
                    return Stream.of((Map<String, String>) new ObjectMapper().readValue(line.trim(), Map.class));
                }
                catch (JsonProcessingException e) {
                    return Stream.of();
                }
            })
            .filter(parsed -> !parsed.get("logger").equals("io.javalin.Javalin"))
            .map(parsed -> String.join(" ",
                parsed.get("time"),
                parsed.get("logger"),
                parsed.get("level"),
                parsed.get("msg")
            ));
    }

    private HttpHeaders headersWithAuth() {
        var headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + token);
        return headers;
    }
}
