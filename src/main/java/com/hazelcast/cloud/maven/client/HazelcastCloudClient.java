package com.hazelcast.cloud.maven.client;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import com.hazelcast.cloud.maven.model.Cluster;

import static com.hazelcast.cloud.maven.auth.AuthUtils.headersWithAuth;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

public class HazelcastCloudClient {

    private final String apiBaseUrl;

    private final String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public HazelcastCloudClient(String apiBaseUrl, String token) {
        this.apiBaseUrl = apiBaseUrl;
        this.token = token;
    }

    public Cluster getClusterStatus(String clusterId) {
        return restTemplate.exchange(
            url("/cluster/{clusterId}"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithAuth(token)),
            Cluster.class,
            clusterId).getBody();
    }

    public Flux<ServerSentEvent<String>> getClusterLogs(String clusterId) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofMinutes(5))))
            .baseUrl(String.format("%s/cluster/%s/logstream", apiBaseUrl, clusterId))
            .build().get()
            .header(AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
            });
    }

    private String temporaryCustomClassUpload(File file) {
        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("customClassesFile", new FileSystemResource(file));

        ResponseEntity<TypesTemporaryCustomClassesResource> resp = restTemplate.postForEntity(
            url("/temporary_custom_classes"),
            new HttpEntity<>(body, headers),
            TypesTemporaryCustomClassesResource.class);
        return resp.getBody().getId();
    }

    public void batchCustomClasses(String clusterId, File file) {
        String id = temporaryCustomClassUpload(file);
        Set<String> tempCCIds = new HashSet<>();
        tempCCIds.add(id);

        HttpHeaders headers = headersWithAuth(token);
        headers.setContentType(APPLICATION_JSON);
        BatchClusterCustomClassesRequest request = BatchClusterCustomClassesRequest.builder()
            .add(BatchClusterCustomClassesRequest.Add.builder().temporaryCustomClassesIds(tempCCIds).build())
            .delete(BatchClusterCustomClassesRequest.Delete.builder().ids(new HashSet<>()).build())
            .build();
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("clusterId", clusterId);
        restTemplate.postForEntity(
            url("/cluster/{clusterId}/custom_classes/batch"),
            new HttpEntity<>(request, headers),
            String.class,
            pathParams);
    }

    private String url(String uri) {
        return this.apiBaseUrl + uri;
    }

}
