package com.atlassian.jira.util;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * An IOException was encountered and the stupid programmer didn't know how to recover, so this got thrown instead.
 */
public class RuntimeIOException extends RuntimeException
{
    private static final long serialVersionUID = -8317205499816761123L;

    public RuntimeIOException(final @Nonnull String message, final @Nonnull IOException cause)
    {
        super(message, cause);
    }

    public RuntimeIOException(final @Nonnull IOException cause)
    {
        super(cause);
    }
}
