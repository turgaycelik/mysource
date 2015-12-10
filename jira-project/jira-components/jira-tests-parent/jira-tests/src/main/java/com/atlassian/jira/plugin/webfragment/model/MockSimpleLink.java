package com.atlassian.jira.plugin.webfragment.model;

/**
 * @since v6.2
 */
public class MockSimpleLink extends MockSimpleLinkSection implements SimpleLink
{

    private String url;
    private String accessKey;

    public MockSimpleLink(final String id)
    {
        super(id);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(final String accessKey)
    {
        this.accessKey = accessKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (!(o instanceof MockSimpleLink)) { return false; }
        if (!super.equals(o)) { return false; }

        final MockSimpleLink that = (MockSimpleLink) o;

        if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null) { return false; }
        if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("MockSimpleLink[%s]", id);
    }
}
