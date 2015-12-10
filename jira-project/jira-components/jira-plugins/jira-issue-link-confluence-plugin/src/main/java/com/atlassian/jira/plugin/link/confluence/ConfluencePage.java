package com.atlassian.jira.plugin.link.confluence;

/**
 * Represents a page on a Confluence instance.
 *
 * @since v5.0
 */
public class ConfluencePage
{
    private final String pageId;
    private final String title;
    private final String url;

    public ConfluencePage(final String pageId, final String title, final String url)
    {
        this.pageId = pageId;
        this.title = title;
        this.url = url;
    }

    public String getPageId()
    {
        return pageId;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

    public static class ConfluencePageBuilder implements Builder<ConfluencePage>
    {
        private String pageId;
        private String title;
        private String url;

        public ConfluencePageBuilder pageId(final String pageId)
        {
            this.pageId = pageId;
            return this;
        }

        public ConfluencePageBuilder title(final String title)
        {
            this.title = title;
            return this;
        }

        public ConfluencePageBuilder url(final String url)
        {
            this.url = url;
            return this;
        }

        public ConfluencePage build()
        {
            return new ConfluencePage(pageId, title, url);
        }

        @Override
        public void clear()
        {
            pageId = null;
            title = null;
            url = null;
        }
    }
}
