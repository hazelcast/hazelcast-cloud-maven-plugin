#Maven Plugin for Hazelcast Cloud

## Plugin execution
```shell
export HZC_API_KEY='***'
export HZC_API_SECRET='***'

mvn -DapiKey=$HZC_API_KEY -DapiSecret=$HZC_API_SECRET clean package hazelcast-cloud:deploy
```
where:
- `HZC_API_KEY` - API key
- `HZC_API_SECRET` - API secret

API key and secret can be generated here https://console-dev.test.hazelcast.cloud/settings/developer