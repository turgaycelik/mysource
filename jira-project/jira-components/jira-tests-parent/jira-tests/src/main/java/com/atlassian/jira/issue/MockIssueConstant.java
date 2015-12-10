package com.atlassian.jira.issue;

import java.util.Locale;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringEscapeUtils;
import com.atlassian.jira.util.I18nHelper;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v3.13
 */
public class MockIssueConstant implements IssueConstant
{
    private final String id;
    private String name;
    private String translatedName;
    private long sequence = 0;
    private String iconUrl = null;
    private String description;
    private GenericValue genericValue;

    public MockIssueConstant(GenericValue genericValue)
    {
        id = genericValue.getString("id");
        name = genericValue.getString("name");
        this.genericValue = genericValue;
    }

    public MockIssueConstant(final String id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(final Long sequence)
    {
        this.sequence = sequence;
    }

    @Override
    public String getCompleteIconUrl()
    {
        return getIconUrl();
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public String getIconUrlHtml()
    {
        return StringEscapeUtils.escapeHtml(getIconUrl());
    }

    public void setIconUrl(final String iconURL)
    {
        this.iconUrl = iconURL;
    }

    public MockIssueConstant url(String url)
    {
        this.iconUrl = url;
        return this;
    }

    public String getNameTranslation()
    {
        return translatedName == null ? name : translatedName;
    }

    public MockIssueConstant setTranslatedName(String translatedName)
    {
        this.translatedName = translatedName;
        return this;
    }

    public String getDescTranslation()
    {
        return description;
    }

    public String getNameTranslation(final String locale)
    {
        return getNameTranslation();
    }

    public String getDescTranslation(final String locale)
    {
        return getDescTranslation();
    }

    public String getNameTranslation(final I18nHelper i18n)
    {
        return getNameTranslation();
    }

    public String getDescTranslation(final I18nHelper i18n)
    {
        return getDescription();
    }

    public void setTranslation(final String translatedName, final String translatedDesc, final String issueConstantPrefix, final Locale locale)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void deleteTranslation(final String issueConstantPrefix, final Locale locale)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public PropertySet getPropertySet()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public int compareTo(final Object o)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockIssueConstant that = (MockIssueConstant) o;

        if (sequence != that.sequence) { return false; }
        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (sequence ^ (sequence >>> 32));
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
