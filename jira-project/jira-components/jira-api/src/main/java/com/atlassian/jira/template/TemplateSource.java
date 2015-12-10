package com.atlassian.jira.template;

import com.atlassian.annotations.ExperimentalApi;

/**
* Defines the source of a {@link org.apache.velocity.Template}
*
* @since v5.1
*/
@ExperimentalApi
public class TemplateSource
{
    public TemplateSource() {}

    /**
     * Defines a template specified in a file.
     */
    @ExperimentalApi
    public static class File extends TemplateSource
    {
        private final String path;

        File(final java.lang.String path)
        {
            this.path = path;
        }

        public String getPath()
        {
            return path;
        }
    }

    /**
     * Defines a template specified in a {@link String} instance.
     */
    @ExperimentalApi
    public static class Fragment extends TemplateSource
    {
        private final String content;

        Fragment(java.lang.String content)
        {
            this.content = content;
        }

        public String getContent()
        {
            return content;
        }
    }
}
