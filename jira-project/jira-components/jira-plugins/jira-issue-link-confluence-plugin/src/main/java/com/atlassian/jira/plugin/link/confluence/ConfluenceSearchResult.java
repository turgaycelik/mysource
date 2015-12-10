package com.atlassian.jira.plugin.link.confluence;

/**
 * Represents an item from a list of search results from a Confluence instance.
 *
 * @since v5.0
 */
public class ConfluenceSearchResult
{
    private final String id;
    private final String type;
    private final String title;
    private final String excerpt;
    private final String url;

    public ConfluenceSearchResult(final String id, final String type, final String title, final String excerpt, final String url)
    {
        this.id = id;
        this.type = type;
        this.title = title;
        this.excerpt = excerpt;
        this.url = url;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public String getTitle()
    {
        return title;
    }

    public String getExcerpt()
    {
        return excerpt;
    }

    public String getUrl()
    {
        return url;
    }

    public static class ConfluenceSearchResultBuilder implements Builder<ConfluenceSearchResult>
    {
        private String id;
        private String type;
        private String title;
        private String excerpt;
        private String url;

        public ConfluenceSearchResultBuilder()
        {
        }

        public ConfluenceSearchResultBuilder(final ConfluenceSearchResult other)
        {
            this.id = other.id;
            this.type = other.type;
            this.title = other.title;
            this.excerpt = other.excerpt;
            this.url = other.url;
        }

        public ConfluenceSearchResultBuilder id(final String id)
        {
            this.id = id;
            return this;
        }

        public ConfluenceSearchResultBuilder type(final String type)
        {
            this.type = type;
            return this;
        }

        public ConfluenceSearchResultBuilder title(final String title)
        {
            this.title = title;
            return this;
        }

        public ConfluenceSearchResultBuilder excerpt(final String excerpt)
        {
            this.excerpt = excerpt;
            return this;
        }

        public ConfluenceSearchResultBuilder url(final String url)
        {
            this.url = url;
            return this;
        }

        @Override
        public ConfluenceSearchResult build()
        {
            return new ConfluenceSearchResult(id, type, title, excerpt, url);
        }

        @Override
        public void clear()
        {
            id = null;
            type = null;
            title = null;
            excerpt = null;
            url = null;
        }
    }
}
