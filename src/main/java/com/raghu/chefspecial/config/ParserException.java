package com.raghu.chefspecial.config;

@SuppressWarnings("serial")
public class ParserException extends RuntimeException
{
    public ParserException(final String message)
    {
        super(message);
    }

    public ParserException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
