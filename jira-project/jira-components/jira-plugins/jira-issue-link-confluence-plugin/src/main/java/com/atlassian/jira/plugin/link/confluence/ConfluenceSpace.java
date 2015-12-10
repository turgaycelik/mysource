package com.atlassian.jira.plugin.link.confluence;

/**
 * Represents a space on a Confluence instance.
 *
 * @since v5.0
 */
public class ConfluenceSpace
{
    private final String key;
    private final String name;
    private final String type;
    private final String url;

    public ConfluenceSpace(final String key, final String name, final String type, final String url)
    {
        this.key = key;
        this.name = name;
        this.type = type;
        this.url = url;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getUrl()
    {
        return url;
    }

    public static class ConfluenceSpaceBuilder implements Builder<ConfluenceSpace>
    {
        private String key;
        private String name;
        private String type;
        private String url;

        public ConfluenceSpaceBuilder key(final String key)
        {
            this.key = key;
            return this;
        }

        public ConfluenceSpaceBuilder name(final String name)
        {
            this.name = name;
            return this;
        }

        public ConfluenceSpaceBuilder type(final String type)
        {
            this.type = type;
            return this;
        }

        public ConfluenceSpaceBuilder url(final String url)
        {
            this.url = url;
            return this;
        }

        @Override
        public ConfluenceSpace build()
        {
            return new ConfluenceSpace(key, name, type, url);
        }

        @Override
        public void clear()
        {
            key = null;
            name = null;
            type = null;
            url = null;
        }
    }
}
