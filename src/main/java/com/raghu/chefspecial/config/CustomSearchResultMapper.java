package com.raghu.chefspecial.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


/**
 * Custom result mapper used for serializing listing search results.
 */
public class CustomSearchResultMapper extends CustomAbstractResultMapper
{
    private static String PROPERTY_LISTING = "property-listing";
    private static String PMC = "pmc";
    /**
     * Build a result mapper with default deserialization settings.
     */
    public static CustomSearchResultMapper buildDefault()
    {
        return new CustomSearchResultMapper(CustomSearchEntityMapper.buildDefault());
    }

    public CustomSearchResultMapper(final CustomSearchEntityMapper entityMapper)
    {
        super(entityMapper);
    }

    public <T> IResultsPage<T> mapResults(
            final SearchResponse response,
            final Class<T> clazz,
            final Pageable pageable)
    {
        final SearchHits searchHits = response.getHits();
        final long totalHits = searchHits.getTotalHits();
        final String scrollId = response.getScrollId();
        final List<T> results = ParallelizationHelper
            .stream(searchHits)
            .filter(hit -> hit != null)
            .map(hit -> {
                T result = null;

                result = this.handleEntityMapping(hit, clazz);


                result = this.setTimeStamp(result, hit);
                result = this.setEntityId(result, hit);
                result = this.setType(result, hit);

                return result;
            })
            .execute();

        final ResultsPageImpl<T> aggregatedPage = new ResultsPageImpl<>(results, pageable, totalHits, scrollId);

        // Process Aggregations.
        if (response.getAggregations() != null) {
            this.processAggregations(response.getAggregations(), aggregatedPage, false);
        }

        return aggregatedPage;
    }

    private <T> T handleEntityMapping(final SearchHit hit, final Class<T> clazz) {
        if (clazz.getName().equalsIgnoreCase("ListingDocument")
                || clazz.getName().equalsIgnoreCase("com.forrent.search.model.ListingDocument")) {
            return this.handlePmcAndPropertingListingResults(hit);
        } else if (StringUtils.isNotBlank(hit.getSourceAsString())) {
            return this.mapEntity(hit.getSourceAsString(), clazz);
        } else {
            return this.mapEntity(this.buildJSONFromFields(hit.getFields().values()), clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T handlePmcAndPropertingListingResults(final SearchHit hit)
    {
        T result = null;

        return result;
    }

    private <T> T setTimeStamp(final T result, final SearchHit hit) {
        String timeStamp = "";
        if (hit.getSource() != null && (hit.getSource().get("@timestamp") != null
                || hit.getSource().get("@saved_at") != null)) {
            timeStamp = hit.getSource().get("@timestamp") != null
                    ? hit.getSource().get("@timestamp").toString()
                            :hit.getSource().get("@saved_at").toString();
        }


        return result;
    }

    private <T> T setEntityId(final T result, final SearchHit hit) {


        return result;
    }

    private <T> T setType(final T result, final SearchHit hit) {

        return result;
    }



    private String buildJSONFromFields(final Collection<SearchHitField> values)
    {
        final JsonFactory nodeFactory = new JsonFactory();
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            for (final SearchHitField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (final Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Extract inner hit data if set.
     *
     * @param hit
     * @param path
     *
     * @return
     */
    private List<Optional<Map<String, Object>>> getInnerHitData(final SearchHit hit, final String path)
    {
        final List<Optional<Map<String, Object>>> hits = new ArrayList<Optional<Map<String, Object>>>();
        try {
            for (int i = 0; i < hit.getInnerHits().get(path).getHits().length; i++) {
                hits.add(Optional.of(hit.getInnerHits().get(path).getAt(i).getSourceAsMap()));
            }
            return hits;
        } catch(final Exception e) {
            return hits;
        }
    }

    private AggregateResult processAggregation(final Aggregation filterAgg)
    {
        final AggregateResult aggregateResult = new AggregateResult();
        return aggregateResult.getResult(filterAgg);
    }

    private <T> void processAggregations(
            final Aggregations responseAggregations,
            final IResultsPage<T> aggregatedPage,
            final boolean fromNested)
    {
        for (final Aggregation aggregation : responseAggregations.asList()) {
            if (aggregation instanceof Nested) {
                final Nested nestedAgg = (Nested) aggregation;
                this.processAggregations(nestedAgg.getAggregations(), aggregatedPage, true);
            } else {
                if (!fromNested) {
                    final Filter filterAggregation = (Filter) aggregation;
                    if (filterAggregation.getAggregations() != null
                            && filterAggregation.getAggregations().asList().size() > 0) {
                        for (final Aggregation internalFilterAgg : filterAggregation.getAggregations().asList()) {
                            aggregatedPage.getAggregates().add(this.processAggregation(internalFilterAgg));
                        }
                    } else {
                        aggregatedPage.getAggregates().add(this.processAggregation(filterAggregation));
                    }

                } else {
                    aggregatedPage.getAggregates().add(this.processAggregation(aggregation));
                }
            }
        }
    }
}
