package com.raghu.chefspecial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class ElasticsearchHostProperties
{
    private String host;

    public String getHost() {
        String host = this.host;
        final String[] hostProperties = this.host.split(":");
        if (this.host.startsWith("http")) {
            host = hostProperties[1].replace("//", "");
        } else {
            host = hostProperties[0];
        }
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getScheme() {
        String scheme = "http";
        if (this.host.startsWith("http")) {
            scheme = this.host.split(":")[0];
        }
        return scheme;
    }

    public Integer getPort() {
        Integer port = 9200;
        final String[] hostProperties = this.host.split(":");
        if (hostProperties.length == 3) {
            port = Integer.parseInt(hostProperties[2].replace("/", ""));
        } else if (hostProperties.length == 2
                && !this.host.startsWith("http")) {
            port = Integer.parseInt(hostProperties[1].replace("/", ""));
        }
        return port;
    }

}
