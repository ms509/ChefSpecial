package com.raghu.chefspecial.config;

public class RangeAggregateBucket extends AggregateBucket
{
    private Integer to;
    private Integer from;

    public Integer getTo() {
        return this.to;
    }

    public void setTo(final Integer to) {
        this.to = to;
    }

    public Integer getFrom() {
        return this.from;
    }

    public void setFrom(final Integer from) {
        this.from = from;
    }
}
