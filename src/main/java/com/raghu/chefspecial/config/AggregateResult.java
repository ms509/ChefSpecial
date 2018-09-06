package com.raghu.chefspecial.config;

import java.util.ArrayList;

import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedReverseNested;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;


public class AggregateResult {

    private String name;
    private ArrayList<AggregateBucket> buckets;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArrayList<AggregateBucket> getBuckets() {
        return this.buckets;
    }

    public void setBuckets(final ArrayList<AggregateBucket> buckets) {
        this.buckets = buckets;
    }

    public AggregateResult getResult(final Aggregation agg) {

        if (agg instanceof Range) {
            final Range aggRange = (Range) agg;
            this.setBuckets(this.getAggreateBuckets(aggRange));
        } else if (agg instanceof Terms) {
            final Terms aggTerm = (Terms) agg;
            this.setBuckets(this.getAggreateBuckets(aggTerm));
        } else if (agg instanceof Histogram) {
            final Histogram aggHistogram = (Histogram) agg;
            this.setBuckets(this.getAggreateBuckets(aggHistogram));
        } else if (agg instanceof InternalFilter) {
            final InternalFilter aggFilter = (InternalFilter) agg;
            final AggregateBucket aggBucket = this.getAggregateBucket("", aggFilter.getDocCount(), null);
            final ArrayList<AggregateBucket> aggregateBuckets = new ArrayList<>();
            aggregateBuckets.add(aggBucket);
            this.setBuckets(aggregateBuckets);
        } else if (agg instanceof ParsedFilter) {
            final ParsedFilter aggFilter = (ParsedFilter) agg;
            final AggregateBucket aggBucket = this.getAggregateBucket("", aggFilter.getDocCount(), null);
            final ArrayList<AggregateBucket> aggregateBuckets = new ArrayList<>();
            aggregateBuckets.add(aggBucket);
            this.setBuckets(aggregateBuckets);
        } else {
            throw new IllegalArgumentException(
                    "Aggregation type not (yet) supported: " + agg.getClass().getName());
        }

        this.setName(agg.getName());
        return this;

    }

    private ArrayList<AggregateBucket> getAggreateBuckets(final Range aggRange) {
        final ArrayList<AggregateBucket> aggregateBuckets = new ArrayList<AggregateBucket>();
        for (final Range.Bucket bucket : aggRange.getBuckets()) {
            final RangeAggregateBucket aggregateBucket = (RangeAggregateBucket) this.getAggregateBucket(bucket.getKeyAsString(), bucket.getDocCount(), bucket.getAggregations());
            aggregateBucket.setFrom(Integer.parseInt(bucket.getFromAsString()));
            aggregateBucket.setTo(Integer.parseInt(bucket.getToAsString()));
            aggregateBuckets.add(aggregateBucket);
        }
        return aggregateBuckets;
    }

    private ArrayList<AggregateBucket> getAggreateBuckets(final Terms aggTerm) {
        final ArrayList<AggregateBucket> aggregateBuckets = new ArrayList<AggregateBucket>();
        for (final Terms.Bucket bucket : aggTerm.getBuckets()) {
            aggregateBuckets.add(this.getAggregateBucket(bucket.getKeyAsString(), bucket.getDocCount(), bucket.getAggregations()));
        }
        return aggregateBuckets;
    }

    private ArrayList<AggregateBucket> getAggreateBuckets(final Histogram aggHistogram) {
        final ArrayList<AggregateBucket> aggregateBuckets = new ArrayList<AggregateBucket>();
        for (final Histogram.Bucket bucket : aggHistogram.getBuckets()) {
            aggregateBuckets.add(this.getAggregateBucket(bucket.getKeyAsString(), bucket.getDocCount(), bucket.getAggregations()));
        }
        return aggregateBuckets;
    }

    private AggregateBucket getAggregateBucket(final String bucketKey, final long docCount, final Aggregations aggregations) {
        final AggregateBucket aggregateBucket = new AggregateBucket();
        final Long reverseNestedSumCount = this.processReverseNestedAggregations(aggregations);
        aggregateBucket.setId(bucketKey);
        aggregateBucket.setCount(reverseNestedSumCount == null ? docCount : reverseNestedSumCount);

        return aggregateBucket;
    }

    private Long processReverseNestedAggregations(final Aggregations reverseNestedAggregations){
        if (reverseNestedAggregations != null && reverseNestedAggregations.asList().size() > 0) {
            for (final Aggregation filterAgg : reverseNestedAggregations.asList()) {
                if (filterAgg instanceof ReverseNested){
                    final ReverseNested reverseNestedAgg = (ReverseNested) filterAgg;
                    return this.sumReverseNestedBuckets(reverseNestedAgg.getAggregations());
                } else if (filterAgg instanceof ParsedReverseNested){
                    final ParsedReverseNested reverseNestedAgg = (ParsedReverseNested) filterAgg;
                    return this.sumReverseNestedBuckets(reverseNestedAgg.getAggregations());
                } else {
                    throw new IllegalArgumentException(
                            "Aggregation type not (yet) supported: " + filterAgg.getClass().getName());
                }
            }
        }

        return null;
    }

    private Long sumReverseNestedBuckets(final Aggregations reverseNestedAggregations) {
        Long totalCount = (long) 0;
        boolean hasAggregations = false;
        for (final Aggregation filterAgg : reverseNestedAggregations.asList()) {
            hasAggregations = true;

            if (filterAgg instanceof Range) {
                final Range aggRange = (Range) filterAgg;
                for (final Range.Bucket bucket : aggRange.getBuckets()) {
                    totalCount += bucket.getDocCount();
                }
            } else if (filterAgg instanceof Terms) {
                final Terms aggTerm = (Terms) filterAgg;
                for (final Terms.Bucket bucket : aggTerm.getBuckets()) {
                    totalCount += bucket.getDocCount();
                }
            } else if (filterAgg instanceof Histogram) {
                final Histogram aggHistogram = (Histogram) filterAgg;
                for (final Histogram.Bucket bucket : aggHistogram.getBuckets()) {
                    totalCount += bucket.getDocCount();
                }
            } else {
                throw new IllegalArgumentException(
                        "Aggregation type not (yet) supported: " + filterAgg.getClass().getName());
            }
        }
        return hasAggregations ? totalCount : null;
    }

}
