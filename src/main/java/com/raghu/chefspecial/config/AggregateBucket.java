package com.raghu.chefspecial.config;

public class AggregateBucket
{
    private Long count;
    private String id;

    public Long getCount() {
        return this.count;
    }

    public String getId() {
        return this.id;
    }

    public void setCount(final Long count) {
        this.count = count;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
