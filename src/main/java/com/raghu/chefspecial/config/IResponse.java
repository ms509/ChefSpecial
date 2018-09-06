package com.raghu.chefspecial.config;

import java.util.Map;

public interface IResponse
{
    /**
     * Get extra information about request/response helpful for debugging.
     */
    Map<String, Object> getDebug();

    void setDebug(Map<String, Object> debug);
}
