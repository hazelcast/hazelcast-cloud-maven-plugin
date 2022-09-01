package com.hazelcast.cloud.maven.auth;

import lombok.experimental.UtilityClass;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.cloud.maven.model.CustomerApiLogin;
import com.hazelcast.cloud.maven.model.CustomerTokenResponse;

@UtilityClass
public class ApiAuthenticator {

    private static final RestTemplate HTTP_CLIENT = new RestTemplate();

    private static volatile String TOKEN_INSTANCE;

    public static synchronized String getToken(String baseURL, String apiKey, String apiSecret) {
        if (TOKEN_INSTANCE != null) {
            return TOKEN_INSTANCE;
        }

        TOKEN_INSTANCE = login(baseURL, apiKey, apiSecret);

        return TOKEN_INSTANCE;
    }

    private static String login(String baseURL, String apiKey, String apiSecret) {
        CustomerApiLogin customerApiLogin = CustomerApiLogin.builder()
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .build();

        return HTTP_CLIENT
            .postForObject(baseURL + "/customers/api/login", customerApiLogin, CustomerTokenResponse.class)
            .getToken();
    }

}
