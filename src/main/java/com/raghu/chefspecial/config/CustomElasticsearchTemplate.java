package com.raghu.chefspecial.config;

import static org.elasticsearch.client.Requests.indicesExistsRequest;
import static org.elasticsearch.client.Requests.refreshRequest;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.util.Assert;
import org.springframework.data.domain.Pageable;


public class CustomElasticsearchTemplate
{
    private final RestClient lowLevelClient;
    private RestHighLevelClient highLevelClient;
    private NodeClient nodeClient;
    private final boolean useEbeddedElastic;
    private final CustomSearchResultMapper resultMapper;

    public CustomElasticsearchTemplate(final RestClient lowLevelClient, final RestHighLevelClient highLevelClient, final CustomSearchResultMapper resultMapper) {
        Assert.notNull(lowLevelClient, "Low Level Rest Client must not be null!");
        Assert.notNull(highLevelClient, "High Level Rest Client must not be null!");
        Assert.notNull(resultMapper, "ResultMapper must not be null!");

        this.lowLevelClient = lowLevelClient;
        this.highLevelClient = highLevelClient;
        this.resultMapper = resultMapper;
        this.useEbeddedElastic = false;
    }

    public CustomElasticsearchTemplate(final RestClient lowLevelClient, final NodeClient nodeClient, final CustomSearchResultMapper resultMapper) {
        Assert.notNull(nodeClient, "Node Client must not be null!");
        Assert.notNull(resultMapper, "ResultMapper must not be null!");

        this.lowLevelClient = lowLevelClient;
        this.nodeClient = nodeClient;
        this.resultMapper = resultMapper;
        this.useEbeddedElastic = true;
    }

    public <T> IResultsPage<T> queryForPage(final CustomSearchRequestBuilder searchRequestBuilder, final Class<T> clazz)
            throws IOException {

        final SearchResponse searchResponse = this.query(searchRequestBuilder);

        return this.resultMapper.mapResults(searchResponse, clazz, searchRequestBuilder.getPageable());
    }

   public SearchResponse query(final CustomSearchRequestBuilder searchRequestBuilder) throws IOException {
       final SearchRequest searchRequest = searchRequestBuilder.build();

       Assert.notEmpty(searchRequest.indices(), "No index defined for Query");
       Assert.notEmpty(searchRequest.types(), "No type defined for Query");

       return this.executeSearch(searchRequest);
   }

   public boolean createIndex(final String indexName, final Object settings) {
       Assert.notNull(indexName, "No index defined for putMapping()");
       Assert.notNull(this.nodeClient, "Node Client must not be null!");
       final CreateIndexRequestBuilder createIndexRequestBuilder = this.nodeClient.admin().indices().prepareCreate(indexName);
       if (settings instanceof String) {
           createIndexRequestBuilder.setSettings(String.valueOf(settings), XContentType.JSON);
       } else if (settings instanceof Map) {
           createIndexRequestBuilder.setSettings((Map) settings);
       } else if (settings instanceof XContentBuilder) {
           createIndexRequestBuilder.setSettings((XContentBuilder) settings);
       }
       return createIndexRequestBuilder.execute().actionGet().isAcknowledged();

   }

   public boolean deleteIndex(final String indexName) {
       Assert.notNull(indexName, "No index defined for delete operation");
       Assert.notNull(this.nodeClient, "Node Client must not be null!");
       if (this.indexExists(indexName)) {
           return this.nodeClient.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet().isAcknowledged();
       }
       return false;
   }

   public boolean putMapping(final String indexName, final String type, final Object mapping) {
       Assert.notNull(indexName, "No index defined for putMapping()");
       Assert.notNull(type, "No type defined for putMapping()");
       Assert.notNull(this.nodeClient, "Node Client must not be null!");
       final PutMappingRequestBuilder requestBuilder = this.nodeClient.admin().indices().preparePutMapping(indexName).setType(type);
       if (mapping instanceof String) {
           requestBuilder.setSource(String.valueOf(mapping), XContentType.JSON);
       } else if (mapping instanceof Map) {
           requestBuilder.setSource((Map) mapping);
       } else if (mapping instanceof XContentBuilder) {
           requestBuilder.setSource((XContentBuilder) mapping);
       }
       return requestBuilder.execute().actionGet().isAcknowledged();
   }

   public String indexDocument(final String indexName, final String indexType, final Object doc, final String docId)
           throws IOException {
       Assert.notNull(indexName, "No index name defined");
       Assert.notNull(indexType, "No index type defined");
       Assert.notNull(this.nodeClient, "Node Client must not be null!");

       IndexRequestBuilder indexRequestBuilder = null;
       if (docId != null) {
           indexRequestBuilder = this.nodeClient.prepareIndex(indexName, indexType, docId);
       } else {
           indexRequestBuilder = this.nodeClient.prepareIndex(indexName, indexType);
       }

       indexRequestBuilder.setSource(this.resultMapper.getEntityMapper().mapToString(doc), XContentType.JSON);

       final String indexedDocId = indexRequestBuilder.execute().actionGet().getId();

       return indexedDocId;
   }

   public void refresh(final String indexName) {
       Assert.notNull(indexName, "No index defined for refresh()");
       Assert.notNull(this.nodeClient, "Node Client must not be null!");
       this.nodeClient.admin().indices().refresh(refreshRequest(indexName)).actionGet();
   }

   public boolean indexExists(final String indexName) {
       Assert.notNull(this.nodeClient, "Node Client must not be null!");
       return this.nodeClient.admin().indices().exists(indicesExistsRequest(indexName)).actionGet().isExists();
   }

   public <T> IResultsPage<T> queryForSlice(final CustomSearchRequestBuilder searchRequestBuilder, final Class<T> clazz, final String scrollId, final int pageSize)
           throws IOException {

       final SearchResponse searchResponse = this.querySlice(searchRequestBuilder, scrollId, pageSize);

       return this.resultMapper.mapResults(searchResponse, clazz, searchRequestBuilder.getPageable());
   }

   public Boolean clearScroll(final String scrollId)
   {
       final ClearScrollRequest request = new ClearScrollRequest();
       request.addScrollId(scrollId);
       try {
           if (!this.useEbeddedElastic) {
               return this.highLevelClient.clearScroll(request).isSucceeded();
           }
           return this.nodeClient.prepareClearScroll().addScrollId(scrollId).get().isSucceeded();
       } catch (final IOException e) {
           e.printStackTrace();
           return false;
       }
   }

   private SearchResponse executeSearch(final SearchRequest searchRequest) throws IOException {
       if (!this.useEbeddedElastic) {
           return this.highLevelClient.search(searchRequest);
       } else {
           return this.nodeClient.execute(SearchAction.INSTANCE, searchRequest).actionGet();
       }
   }

   private SearchResponse querySlice(final CustomSearchRequestBuilder searchRequestBuilder, final String scrollId, final int pageSize) throws IOException {
       final SearchRequest searchRequest = searchRequestBuilder.build();

       Assert.notEmpty(searchRequest.indices(), "No index defined for Query");
       Assert.notEmpty(searchRequest.types(), "No type defined for Query");

       return this.executeScrollSearch(searchRequest, scrollId, pageSize);
   }

    private SearchResponse executeScrollSearch(final SearchRequest searchRequest, final String scrollId, final int pageSize) throws IOException
    {
        SearchScrollRequest scrollRequest = null;
        Boolean scrollIdExists = false;

        if (scrollId != null && !scrollId.equals("")) {
            scrollIdExists = true;
            scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
        } else {
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));
            searchRequest.source().size(pageSize);
        }

        if (!this.useEbeddedElastic) {
            if (scrollIdExists) {
                return this.highLevelClient.searchScroll(scrollRequest);
            }
            return this.highLevelClient.search(searchRequest);
        } else {
            if (scrollIdExists) {
                return this.nodeClient.prepareSearchScroll(scrollId).get();
            }
            return this.nodeClient.execute(SearchAction.INSTANCE, searchRequest).actionGet();
        }
    }
}
