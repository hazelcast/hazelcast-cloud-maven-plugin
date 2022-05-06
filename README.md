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
            <version>0.4-SNAPSHOT</version>
            <configuration>
                <apiBaseUrl>https://coordinator.hazelcast.cloud</apiBaseUrl>
                <clusterName>de-1234</clusterName>
                <apiKey>${apiKey}</apiKey>
                <apiSecret>${apiSecret}</apiSecret>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration

| Parameter   | Description                                                 |
|-------------|-------------------------------------------------------------|
| apiBaseUrl  | API base URL (example: https://coordinator.hazelcast.cloud) |
| clusterName | Cluster name (example: de-1234)                             |
| apiKey      | API key*                                                    |
| apiSecret   | API secret*                                                 |

*API key and secret can be generated here https://cloud.hazelcast.com/settings/developer

## Goals
| Goal | Description |
| --- | --- |
| deploy | Upload classes in the module artifact (jar) as a custom classes to the cluster |

## Execution example
```shell
mvn clean package hazelcast-cloud:deploy
```