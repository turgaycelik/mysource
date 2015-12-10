package com.atlassian.jira.entity.property;

import javax.annotation.Nonnull;

/**
 * Indicates that the {@code String} value supplied as a JSON {@link EntityProperty}
 * could not be accepted because it is malformed.
 *
 * @since v6.1
 */
public class InvalidJsonPropertyException extends IllegalArgumentException
{
    InvalidJsonPropertyException(@Nonnull Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
