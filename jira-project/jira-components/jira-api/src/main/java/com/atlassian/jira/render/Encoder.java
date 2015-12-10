package com.atlassian.jira.render;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.PublicApi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encoder used to render content that has been provided by JIRA administrators. Depending on the security profile that
 * is in use, this content may or may not be HTML-encoded when being displayed.
 *
 * @since v5.0.4
 */
@PublicApi
@ExperimentalApi
public interface Encoder
{
    /**
     * Renders <code>input</code> as HTML. This method calls toString() on <code>input</code> in order to get its
     * String representation. If <code>input</code> is null, this method returns "".
     *
     * @param input a String
     * @return a String that is encoded for HTML
     */
    @Nonnull
    String encodeForHtml(@Nullable Object input);
}
