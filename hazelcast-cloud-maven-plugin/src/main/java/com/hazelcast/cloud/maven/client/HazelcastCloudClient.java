package com.hazelcast.cloud.maven.client;

import com.hazelcast.cloud.maven.model.Cluster;
import com.hazelcast.cloud.maven.model.CustomerApiLogin;
import com.hazelcast.cloud.maven.model.CustomerTokenResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

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

    public String getCluster(String clusterId) {
        var headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + token);

        var requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(
          url("/cluster/{clusterId}"),
          HttpMethod.GET,
          requestEntity,
          String.class,
          clusterId).getBody();
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
    public Cluster getClusterStatus(String clusterId) {
        var headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + token);

        var requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(
          url("/cluster/{clusterId}"),
          HttpMethod.GET,
          requestEntity,
          Cluster.class,
          clusterId).getBody();
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
