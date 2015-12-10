package com.atlassian.jira.template;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Static factory methods for {@link TemplateSource} instances.
 *
 * Designed to be used with static import for improved readability of your code.
 *
 * @since v5.1
 */
@ExperimentalApi
public class TemplateSources
{
    @ExperimentalApi
    public static TemplateSource file(final String path)
    {
        return new TemplateSource.File(path);
    }

    @ExperimentalApi
    public static TemplateSource fragment(final String content)
    {
        return new TemplateSource.Fragment(content);
    }
}
