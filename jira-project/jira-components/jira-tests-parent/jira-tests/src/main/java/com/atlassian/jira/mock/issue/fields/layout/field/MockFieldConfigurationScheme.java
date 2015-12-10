package com.atlassian.jira.mock.issue.fields.layout.field;

import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Mock for {@link FieldConfigurationScheme} that compares equality
 * based on id and name
 *
 * @since v4.4
 */
public class MockFieldConfigurationScheme implements FieldConfigurationScheme
{
    private Long id;
    private String name;
    private String description;
    private Map<String, Long> issueTypeToFieldLayoutIdMapping;

    public MockFieldConfigurationScheme()
    {
    }

    public MockFieldConfigurationScheme(final Long id, final String name, final String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public MockFieldConfigurationScheme setId(Long id)
    {
        this.id = id;
        return this;
    }

    public MockFieldConfigurationScheme setName(String name)
    {
        this.name = name;
        return this;
    }

    public MockFieldConfigurationScheme setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Long getFieldLayoutId(String issueTypeId)
    {
        return issueTypeToFieldLayoutIdMapping.get(issueTypeId);
    }

    public Set<Long> getAllFieldLayoutIds(Collection<String> allIssueTypeIds)
    {
        return null;
    }

    public MockFieldConfigurationScheme setIssueTypeToFieldLayoutIdMapping(Map<String, Long> issueTypeToFieldLayoutIdMapping)
    {
        this.issueTypeToFieldLayoutIdMapping = issueTypeToFieldLayoutIdMapping;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockFieldConfigurationScheme that = (MockFieldConfigurationScheme) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
