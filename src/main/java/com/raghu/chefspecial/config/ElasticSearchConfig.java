package com.raghu.chefspecial.config;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.NodeSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

	@Bean
	public ElasticSearchTemplate customElasticsearchTemplate() {
		return new ElasticSearchTemplate(getHighLevelClient(), getLowLevelClient());
	}

	private RestClient getLowLevelClient() {
		RestClient restClient = builder().build();

		return restClient;
	}

	private RestHighLevelClient getHighLevelClient() {
		RestHighLevelClient client = new RestHighLevelClient(this.builder());
		return client;
	}

	private RestClientBuilder builder() {
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
		builder.setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS);
		builder.setFailureListener(new RestClient.FailureListener() {
			@Override
			public void onFailure(Node node) {
				System.out.println(node.getAttributes());
			}
		});
		builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
			@Override
			public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
				return requestConfigBuilder.setSocketTimeout(10000);
			}
		});

//		builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//			@Override
//			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//				return httpClientBuilder.setProxy(new HttpHost("proxy", 9000, "http"));
//			}
//		});
		return builder;
	}
}
