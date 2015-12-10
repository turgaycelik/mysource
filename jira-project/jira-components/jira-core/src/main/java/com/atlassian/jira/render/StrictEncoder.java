package com.atlassian.jira.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * Encoder that encodes everything.
 *
 * @since v5.0.4
 */
@Immutable
public final class StrictEncoder implements Encoder
{
    @Override
    @Nonnull
    public String encodeForHtml(@Nullable Object input)
    {
        return input != null ? htmlEncode(input.toString()) : "";
    }
}
