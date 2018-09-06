package com.raghu.chefspecial.config;

import java.util.ArrayList;

import org.springframework.data.domain.Page;

public interface IResultsPage<T> extends IResponse, Page<T>
{
    /**
     * Get calculated aggregation for this result set.
     */
    ArrayList<AggregateResult> getAggregates();

    /**
     * Get the seed number used for randomization.
     */
    Integer getRandomSeed();

    /**
     * Get the scroll id for current request.
     */
    String getScrollId();
}
