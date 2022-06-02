# Maven Plugin for Hazelcast Cloud

## Usage example

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.hazelcast.cloud</groupId>
            <artifactId>hazelcast-cloud-maven-plugin</artifactId>
            <version>0.0.1</version>
            <configuration>
                <clusterName>de-1234</clusterName>
                <apiKey>${apiKey}</apiKey>
                <apiSecret>${apiSecret}</apiSecret>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration

| Parameter  | Description                                                           |
|-------------|--------------------------------------------------------------------------------|
| apiBaseUrl  | API base URL (optional, default: https://coordinator.hazelcast.cloud) |
| clusterName | Cluster name (example: de-1234)                                                            |
| apiKey | API key*                                                              |
| apiSecret| API secret*                                                           |

*API key and secret can be generated in your account developer's settings https://cloud.hazelcast.com/settings/developer

## Goals
| Goal | Description                                                                      |
| --- |----------------------------------------------------------------------------------|
| deploy | Upload classes in the module's artifact (jar) as a custom classes to the cluster |

## Execution example
```shell
mvn clean package hazelcast-cloud:deploy
```