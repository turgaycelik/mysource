package com.atlassian.jira.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Encoder that renders everything as-is.
 *
 * @since v5.0.4
 */
@Immutable
public final class NoOpEncoder implements Encoder
{
    @Override
    @Nonnull
    public String encodeForHtml(@Nullable Object input)
    {
        return input != null ? input.toString() : "";
    }
}
