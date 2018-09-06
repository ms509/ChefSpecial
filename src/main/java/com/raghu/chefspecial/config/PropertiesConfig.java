package com.raghu.chefspecial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "search-api")
public class PropertiesConfig {
    /**
     * Should application use an embedded ElasticSearch instance created at start up.
     * Used by end to end tests.
     */
    private Boolean useEmbeddedElastic = false;

    /**
     * Should application attempt to create ElasticSearch indices on startup.
     * Used by end to end tests.
     */
    private Boolean createElasticIndicies = false;

    /**
     * Should ElasticSearch indices be seeded with dummy data at start up.
     * Used by end to end tests.
     */
    private Boolean seedElasticIndicies = false;

    private String host;
    private String thumbsServer;
    private String imagesServer;
    private String listingPinAcceptHeader;

    public Boolean getCreateElasticIndicies() {
        return this.createElasticIndicies;
    }

    public String getHost() {
        return this.host;
    }

    public Boolean getSeedElasticIndicies() {
        return this.seedElasticIndicies;
    }

    public String getThumbsServer() {
        return this.thumbsServer;
    }

    public String getImagesServer() {
        return this.imagesServer;
    }

    public Boolean getUseEmbeddedElastic()
    {
        return this.useEmbeddedElastic;
    }

    public String getListingPinAcceptHeader()
    {
        return this.listingPinAcceptHeader;
    }

    public void setCreateElasticIndicies(final Boolean createElasticIndicies) {
        this.createElasticIndicies = createElasticIndicies;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setSeedElasticIndicies(final Boolean seedElasticIndicies) {
        this.seedElasticIndicies = seedElasticIndicies;
    }

    public void setThumbsServer(final String thumbsServer) {
        this.thumbsServer = thumbsServer;
    }

    public void setImagesServer(final String imagesServer) {
        this.imagesServer = imagesServer;
    }

    public void setUseEmbeddedElastic(final Boolean embeddedTestElastic)
    {
        this.useEmbeddedElastic = embeddedTestElastic;
    }

    public void setListingPinAcceptHeader(final String listingPinAcceptHeader)
    {
        this.listingPinAcceptHeader = listingPinAcceptHeader;
    }
}
