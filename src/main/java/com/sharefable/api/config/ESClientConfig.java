package com.sharefable.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
@ConfigurationProperties(prefix="com.sharefable.api.es")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ESClientConfig extends AbstractElasticsearchConfiguration {
    private String url;

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration config = ClientConfiguration.builder()
            .connectedTo(url)
            .usingSsl()
            .withBasicAuth("elastic", "IkVwCchnk7DqxYJ6QlVtidnW")
            .build();

        return RestClients.create(config).rest();
    }
}
