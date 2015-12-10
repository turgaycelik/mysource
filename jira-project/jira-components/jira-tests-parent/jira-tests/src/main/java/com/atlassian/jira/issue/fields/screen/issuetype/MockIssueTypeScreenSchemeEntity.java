package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.MockFieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;

import com.google.common.primitives.Longs;

import org.ofbiz.core.entity.GenericValue;

/**
 * Simple implementation of a {@link IssueTypeScreenSchemeEntity} for tests.
 *
 * @since v6.2
 */
public class MockIssueTypeScreenSchemeEntity implements IssueTypeScreenSchemeEntity
{
    private long id;
    private IssueType issueType;
    private FieldScreenScheme fieldScreenScheme;
    private IssueTypeScreenScheme parent;

    @Override
    public Long getId()
    {
        return id;
    }

    public MockIssueTypeScreenSchemeEntity id(long id)
    {
        this.id = id;
        return this;
    }

    @Override
    public String getIssueTypeId()
    {
        return issueType != null ? issueType.getId() : null;
    }

    public MockIssueTypeScreenSchemeEntity issueType(IssueType type)
    {
        this.issueType = type;
        return this;
    }

    @Override
    public GenericValue getIssueType()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public IssueType getIssueTypeObject()
    {
        return issueType;
    }

    @Override
    public void setIssueTypeId(final String issueTypeId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FieldScreenScheme getFieldScreenScheme()
    {
        return fieldScreenScheme;
    }

    @Override
    public void setFieldScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        this.fieldScreenScheme = fieldScreenScheme;
    }

    public MockIssueTypeScreenSchemeEntity fieldScreenScheme(FieldScreenScheme scheme)
    {
        this.fieldScreenScheme = scheme;
        return this;
    }

    public MockFieldScreenScheme createFieldScreenScheme(long id)
    {
        final MockFieldScreenScheme mockFieldScreenScheme = new MockFieldScreenScheme();
        mockFieldScreenScheme.setId(id);
        this.fieldScreenScheme = mockFieldScreenScheme;
        return mockFieldScreenScheme;
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        return parent;
    }

    public MockIssueTypeScreenSchemeEntity issueTypeScreenScheme(IssueTypeScreenScheme parent)
    {
        this.parent = parent;
        return this;
    }

    @Override
    public void setIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        parent = issueTypeScreenScheme;
    }

    @Override
    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setGenericValue(final GenericValue genericValue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void store()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Long getFieldScreenSchemeId()
    {
        return getFieldScreenScheme().getId();
    }

    @Override
    public int compareTo(final IssueTypeScreenSchemeEntity o)
    {
        return Longs.compare(id, o.getId());
    }
}
