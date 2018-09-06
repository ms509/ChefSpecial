package com.raghu.chefspecial.config;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;


public class CustomAbstractResultMapper
{
    private final CustomSearchEntityMapper entityMapper;

    public CustomAbstractResultMapper(final CustomSearchEntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    public <T> T mapEntity(final String source, final Class<T> clazz) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        try {
            return this.entityMapper.mapToObject(source, clazz);
        } catch (final IOException e) {
            throw new ParserException("failed to map source [ " + source + "] to class " + clazz.getSimpleName(), e);
        }
    }

    public CustomSearchEntityMapper getEntityMapper() {
        return this.entityMapper;
    }

}
