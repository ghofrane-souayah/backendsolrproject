package com.example.usermangment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(SolrMonitoringProperties.class)
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(SolrMonitoringProperties props) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(props.getTimeouts().getConnectMs());
        f.setReadTimeout(props.getTimeouts().getReadMs());
        return new RestTemplate(f);
    }
}