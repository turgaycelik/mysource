package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;

/**
 * A simple mock implementation of the {@link com.atlassian.jira.issue.search.searchers.IssueSearcher} interface for
 * testsing.
 *
 * @since v4.0
 */
public class MockIssueSearcher<T extends SearchableField> implements IssueSearcher<T>
{
    private final String id;
    private SearcherInformation<T> information;
    private SearchRenderer renderer;
    private T field;

    public MockIssueSearcher(final String id)
    {
        this.id = id;
        this.information = new MockSearcherInformation<T>(id);
    }

    public String getId()
    {
        return id;
    }

    public void setInformation(final SearcherInformation<T> information)
    {
        this.information = information;
    }

    public void init(final T field)
    {
        this.field = field;
    }

    public T getField()
    {
        return field;
    }

    public SearcherInformation<T> getSearchInformation()
    {
        return information;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        throw new UnsupportedOperationException();
    }

    public SearchRenderer getSearchRenderer()
    {
        return renderer;
    }

    public void setRenderer(final SearchRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockIssueSearcher that = (MockIssueSearcher) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
