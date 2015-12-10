package com.atlassian.jira.hints;

import com.atlassian.annotations.PublicApi;

/**
 * Holds hint data. Currently only the text of a hint, might be extended
 * in the future.
 *
 * @since v4.2 (Interface extracted from class in v5.0)
 */
@PublicApi
public interface Hint
{
    String getText();

    String getTooltip();
}
