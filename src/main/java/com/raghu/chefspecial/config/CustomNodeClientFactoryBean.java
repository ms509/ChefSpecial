package com.raghu.chefspecial.config;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class CustomNodeClientFactoryBean implements FactoryBean<Client>, InitializingBean, DisposableBean
{
    private static final Logger logger = LoggerFactory.getLogger(CustomNodeClientFactoryBean.class);
    private boolean local;
    private boolean enableHttp;
    private String clusterName;
    private NodeClient nodeClient;
    private String pathData;
    private String pathHome;
    private String pathConfiguration;

    public static class TestNode extends Node {
        public TestNode(final Settings preparedSettings, final Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
    }

    CustomNodeClientFactoryBean() {
    }

    public CustomNodeClientFactoryBean(final boolean local) {
        this.local = local;
    }

    @Override
    public NodeClient getObject() throws Exception {
        return this.nodeClient;
    }

    @Override
    public Class<? extends Client> getObjectType() {
        return NodeClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        this.nodeClient = (NodeClient) new TestNode(
                Settings.builder().put(this.loadConfig())
                        .put("http.enabled", this.enableHttp)
                        .put("transport.type", "netty4")
                        .put("transport.type", "local")
                        .put("http.type", "netty4")
                        .put("path.home", this.pathHome)
                        .put("path.data", this.pathData)
                        .put("cluster.name", this.clusterName)
                        .put("node.max_local_storage_nodes", 100)
                        .put("script.inline", "true")
                        .build(), asList(Netty4Plugin.class)).start().client();
    }

    private Settings loadConfig() throws IOException {
        if (StringUtils.isNotBlank(this.pathConfiguration)) {
            final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(this.pathConfiguration);
            if (stream != null) {
                return Settings.builder().loadFromStream(this.pathConfiguration, this.getClass().getClassLoader().getResourceAsStream(this.pathConfiguration)).build();
            }
            logger.error(String.format("Unable to read node configuration from file [%s]", this.pathConfiguration));
        }
        return Settings.builder().build();
    }

    public void setLocal(final boolean local) {
        this.local = local;
    }

    public void setEnableHttp(final boolean enableHttp) {
        this.enableHttp = enableHttp;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public void setPathData(final String pathData) {
        this.pathData = pathData;
    }

    public void setPathHome(final String pathHome) {
        this.pathHome = pathHome;
    }

    public void setPathConfiguration(final String configuration) {
        this.pathConfiguration = configuration;
    }

    @Override
    public void destroy() throws Exception {
        try {
            logger.info("Closing elasticSearch  client");
            if (this.nodeClient != null) {
                this.nodeClient.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing ElasticSearch client: ", e);
        }
    }

}
