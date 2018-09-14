package com.raghu.chefspecial.config;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

public class ElasticSearchTemplate {

	RestHighLevelClient client;
	RestClient lowLevelClient;

	public ElasticSearchTemplate(RestHighLevelClient client, RestClient lowLevelClient) {
		this.client = client;
		this.lowLevelClient = lowLevelClient;
	}

	public void createIndex(String indexName, String mappings, String settings) {
		// TODO Auto-generated method stub

	}

	public void putMapping(String indexName, String type, String mappings) {
		// TODO Auto-generated method stub

	}

	public void refresh(String indexName) {
		// TODO Auto-generated method stub

	}

	public void index() throws IOException {
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		IndexRequest indexRequest = new IndexRequest("posts", "doc", "1").source(jsonMap);
		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		System.out.println(indexResponse);
	}

	public Boolean createIndex(String mappings, String indexName) throws IOException {
		CreateIndexRequest request = new CreateIndexRequest(indexName);
		request.source(mappings, XContentType.JSON);

		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		if (createIndexResponse.isAcknowledged()) {
			return true;
		}
		return false;
	}

}
