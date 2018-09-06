package com.raghu.chefspecial.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;

import com.vividsolutions.jts.util.Assert;

public class CustomSearchRequestBuilder
{
    private QueryBuilder queryBuilder;
    private final List<SortBuilder> sortBuilders = new ArrayList<>();
    private final List<AbstractAggregationBuilder> aggregationBuilders = new ArrayList<>();
    private HighlightBuilder.Field[] highlightFields = new HighlightBuilder.Field[0];
    private Pageable pageable = Pageable.unpaged();
    private int size = 10;
    private int from = 0;
    private String[] indices = new String[0];
    private String[] types = new String[0];
    private String[] includeFields = new String[0];
    private String[] excludeFields = new String[0];
    private float minScore;
    private SearchType searchType;
    private String customAggregationQuery;

    public Pageable getPageable() {
        return this.pageable;
    }

    public CustomSearchRequestBuilder withQuery(final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        return this;
    }
    
    public CustomSearchRequestBuilder withQuery(final String queryJsonString) throws IOException {
        this.queryBuilder = QueryBuilders.wrapperQuery(queryJsonString);
        return this;
    }

    public CustomSearchRequestBuilder withSort(final SortBuilder sortBuilder) {
        this.sortBuilders.add(sortBuilder);
        return this;
    }

    public CustomSearchRequestBuilder addAggregation(final AbstractAggregationBuilder aggregationBuilder) {
        this.aggregationBuilders.add(aggregationBuilder);
        return this;
    }
    
    public CustomSearchRequestBuilder addAggregations(final List<AbstractAggregationBuilder> aggregationBuilders) {
        this.aggregationBuilders.addAll(aggregationBuilders);
        return this;
    }

    public CustomSearchRequestBuilder withHighlightFields(final HighlightBuilder.Field... highlightFields) {
        this.highlightFields = highlightFields;
        return this;
    }

    public CustomSearchRequestBuilder withPageable(final Pageable pageable) {
    	Assert.isTrue(pageable.isPaged(), "Pageable is not Paged");
    	this.pageable = pageable;
        this.from = pageable.getPageNumber() * pageable.getPageSize();
        this.size = pageable.getPageSize();
        return this;
    }

    public CustomSearchRequestBuilder withSize(final int size) {
        this.size = size;
        return this;
    }
    
    public CustomSearchRequestBuilder withFrom(final int from) {
        this.from = from;
        return this;
    }

    public CustomSearchRequestBuilder withIndices(final String... indices) {
        this.indices = indices;
        return this;
    }

    public CustomSearchRequestBuilder withTypes(final String... types) {
        this.types = types;
        return this;
    }

    public CustomSearchRequestBuilder withIncludeFields(final String... fields) {
        this.includeFields = fields;
        return this;
    }

    public CustomSearchRequestBuilder withExcludeFields(final String... fields) {
        this.excludeFields = fields;
        return this;
    }

    public CustomSearchRequestBuilder withMinScore(final float minScore) {
        this.minScore = minScore;
        return this;
    }

    public CustomSearchRequestBuilder withSearchType(final SearchType searchType) {
        this.searchType = searchType;
        return this;
    }

    public CustomSearchRequestBuilder withCustomAggregationQuery(final String customAggregationQuery){
        this.customAggregationQuery = customAggregationQuery;
        return this;
    }

    public SearchRequest build() {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        int startRecord = this.from;
        int size = this.size;
        
        sourceBuilder.from(startRecord);
        sourceBuilder.size(size);

        if (this.queryBuilder != null) {
            sourceBuilder.query(this.queryBuilder);
        }

        if (!this.sortBuilders.isEmpty()) {
            for (final SortBuilder sortBuilder : this.sortBuilders) {
                sourceBuilder.sort(sortBuilder);
            }
        }

        if (!this.aggregationBuilders.isEmpty()) {
            for (final AbstractAggregationBuilder aggregationBuilder : this.aggregationBuilders) {
                sourceBuilder.aggregation(aggregationBuilder);
            }
        } else if (this.customAggregationQuery != null && !this.customAggregationQuery.isEmpty()){
            try {
                SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
                XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), this.customAggregationQuery);
                sourceBuilder.parseXContent(new QueryParseContext(parser));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.highlightFields.length > 0) {
            for (final HighlightBuilder.Field highlightField : this.highlightFields) {
                sourceBuilder.highlighter(new HighlightBuilder().field(highlightField));
            }
        }

        if (this.minScore > 0) {
            sourceBuilder.minScore(this.minScore);
        }

        final String[] includes = (this.includeFields.length > 0) ? this.includeFields : new String[0];
        final String[] excludes = (this.excludeFields.length > 0) ? this.excludeFields : new String[0];

        sourceBuilder.fetchSource(includes,excludes);

        final SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(sourceBuilder);

        if (this.indices.length > 0) {
            searchRequest.indices(this.indices);
        }

        if (this.types.length > 0) {
            searchRequest.types(this.types);
        }

        if (this.searchType != null) {
            searchRequest.searchType(this.searchType);
        }

        return searchRequest;
    }

}
