package com.raghu.chefspecial.config;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom entity mapper used for serializing listing search results.
 */
public class CustomSearchEntityMapper
{
    /**
     * Build an entity mapper with default deserialization settings.
     */
    public static CustomSearchEntityMapper buildDefault()
    {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        return new CustomSearchEntityMapper(objectMapper);
    }

    private final ObjectMapper objectMapper;

    public CustomSearchEntityMapper(final ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public <T> T mapToObject(final String source, final Class<T> clazz) throws IOException
    {
        return this.objectMapper.readValue(source, clazz);
    }

    public String mapToString(final Object object) throws IOException
    {
        return this.objectMapper.writeValueAsString(object);
    }

    public <T> T convertToObject(final Object source, final Class<T> clazz)
    {
        return this.objectMapper.convertValue(source, clazz);
    }
}
