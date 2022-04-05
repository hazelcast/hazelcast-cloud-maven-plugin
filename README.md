# Maven Plugin for Hazelcast Cloud

## Usage example

```xml
<pluginRepositories>
    <pluginRepository>
        <id>github</id>
        <name>Hazelcast Maven Packages</name>
        <url>https://maven.pkg.github.com/hazelcast/hazelcast-cloud-maven-plugin</url>
    </pluginRepository>
</pluginRepositories>

<build>
    <plugins>
        <plugin>
            <groupId>com.hazelcast.cloud</groupId>
            <artifactId>hazelcast-cloud-maven-plugin</artifactId>
            <version>0.2-SNAPSHOT</version>
            <configuration>
                <apiBaseUrl>https://coordinator.hazelcast.cloud</apiBaseUrl>
                <clusterId>1234</clusterId>
                <apiKey>${apiKey}</apiKey>
                <apiSecret>${apiSecret}</apiSecret>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration

| Parameter  | Description                                        |
|------------|----------------------------------------------------|
| apiBaseUrl | API base URL (example: https://coordinator.hazelcast.cloud) |
| clusterId | Cluster Id                                         |
| apiKey | API key*                                           |
| apiSecret| API secret*                                        |

*API key and secret can be generated here https://cloud.hazelcast.com/settings/developer

## Goals
| Goal | Description |
| --- | --- |
| deploy | Upload classes in the module artifact (jar) as a custom classes to the cluster |

## Execution example
```shell
mvn clean package hazelcast-cloud:deploy
```