package com.raghu.chefspecial.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.lease.Releasable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ElasticsearchConfig implements DisposableBean {
	/**
	 * If running Elasticsearch as embedded node, data files will be written to this
	 * path.
	 */
	private static final String DATA_DIRECTORY = "build/data";

	@Autowired
	private PropertiesConfig properties;

	@Autowired
	private ElasticsearchHostProperties elasticsearchHostProperties;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Releasable releasable;

	private RestClient lowLevelRestClient;

	@Override
	public void destroy() throws Exception {
		this.log.info("Closing connection to Elasticsearch.");

		if (this.releasable != null) {
			this.log.info("Closing releasable.");
			this.releasable.close();
		}

		if (this.lowLevelRestClient != null) {
			this.lowLevelRestClient.close();
		}

		if (this.properties.getUseEmbeddedElastic()) {
			this.log.info(String.format("Cleaning data directory at %s.", DATA_DIRECTORY));
			this.deleteDirectory(DATA_DIRECTORY);
		}
	}

	@Bean
	public NodeClient elasticsearchTestNodeClient() throws Exception {
		NodeClient client = null;

		if (this.properties.getUseEmbeddedElastic()) {
			this.log.info(String.format("Starting local Elasticsearch instance in %s.", DATA_DIRECTORY));

			final CustomNodeClientFactoryBean factory = new CustomNodeClientFactoryBean(true);
			factory.setPathData(DATA_DIRECTORY);
			factory.setEnableHttp(false);
			factory.setPathHome(".");
			factory.setClusterName("elasticsearch");
			factory.afterPropertiesSet();
			client = factory.getObject();

			this.releasable = client;
		}

		return client;
	}

	public RestClient elasticLowLevelClient() {

		if (!this.properties.getUseEmbeddedElastic()) {
			this.log.info(String.format("Connecting to remote Elasticsearch instance at %s.",
					this.elasticsearchHostProperties.getHost()));
			if (this.lowLevelRestClient != null) {
				return this.lowLevelRestClient;
			}
			final Header[] defaultHeaders = new Header[] { new BasicHeader("Content-Type", "application/json") };
			this.lowLevelRestClient = RestClient
					.builder(new HttpHost(this.elasticsearchHostProperties.getHost(),
							this.elasticsearchHostProperties.getPort(), this.elasticsearchHostProperties.getScheme()))
					.setDefaultHeaders(defaultHeaders).build();
		}

		return this.lowLevelRestClient;
	}

	@Bean
	public RestHighLevelClient elasticsearchHighLevelClient() {
		RestHighLevelClient highLevelClient = null;
		if (!this.properties.getUseEmbeddedElastic()) {
			highLevelClient = new RestHighLevelClient(this.elasticLowLevelClient());
		}

		return highLevelClient;
	}

	@Bean
	public CustomElasticsearchTemplate customElasticsearchTemplate() throws Exception {
			return new CustomElasticsearchTemplate(this.elasticLowLevelClient(), this.elasticsearchHighLevelClient(),
					CustomSearchResultMapper.buildDefault());

	}

	@Bean
	public Boolean IsUsingEmbeddedElastic() {
		return this.properties.getUseEmbeddedElastic();
	}

	private void deleteDirectory(final String path) {
		try {
			FileUtils.deleteDirectory(new File(path));
		} catch (final IOException e) {
			throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
		}
	}

	private String appendPortNumber(final String clusterNodes) {
		final String[] clusterNodesList = clusterNodes.split(",");
		final List<String> clusterNodesWithPortList = new ArrayList<>();

		for (String clusterNode : clusterNodesList) {
			if (clusterNode.split(":").length == 1) {
				clusterNode = clusterNode.trim() + ":9300";
			}

			clusterNodesWithPortList.add(clusterNode);
		}

		return String.join(",", clusterNodesWithPortList);
	}
}
