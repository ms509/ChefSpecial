package com.raghu.chefspecial.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@SuppressWarnings({ "serial" })
public class ResultsPageImpl<T> extends PageImpl<T> implements IResultsPage<T>
{
    private ArrayList<AggregateResult> aggregates = new ArrayList<>();
    private Map<String, Object> debug = new HashMap<>();
    private Integer randomSeed;
    private String scrollId;

    public ResultsPageImpl()
    {
        super(new ArrayList<>());
    }

    public ResultsPageImpl(final List<T> content)
    {
        super(content);
    }

    public ResultsPageImpl(final List<T> content, final Pageable pageable, final long total)
    {
        super(content, pageable, total);
    }

    public ResultsPageImpl(final List<T> content, final Pageable pageable, final long total, final String scrollId)
    {
        super(content, pageable, total);
        this.scrollId = scrollId;
    }

    public ResultsPageImpl(final List<T> content, final Pageable pageable, final long total,
            final ArrayList<AggregateResult> aggregates, final Integer randomSeed, final String scrollId)
    {
        super(content, pageable, total);
        this.aggregates = aggregates;
        this.randomSeed = randomSeed;
        this.scrollId = scrollId;
    }

    public ResultsPageImpl(final List<T> content, final Pageable pageable, final long total,
            final ArrayList<AggregateResult> aggregates, final Integer randomSeed, final Map<String, Object> debug, final String scrollId)
    {
        super(content, pageable, total);
        this.aggregates = aggregates;
        this.randomSeed = randomSeed;
        this.debug = debug;
        this.scrollId = scrollId;
    }

    @Override
    public ArrayList<AggregateResult> getAggregates()
    {
        return this.aggregates;
    }

    @Override
    public Map<String, Object> getDebug()
    {
        return this.debug;
    }

    @Override
    public Integer getRandomSeed()
    {
        return this.randomSeed;
    }

    @Override
    public void setDebug(final Map<String, Object> debug)
    {
        this.debug = debug;
    }

    @Override
    public String getScrollId()
    {
        return this.scrollId;
    }
}
