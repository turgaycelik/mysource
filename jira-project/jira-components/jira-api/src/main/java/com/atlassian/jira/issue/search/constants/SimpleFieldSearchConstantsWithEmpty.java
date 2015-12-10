package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.IndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.query.operator.Operator;
import com.atlassian.util.concurrent.LazyReference;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Holds searching constants for simple system fields.
 *
 * @since v4.0
 */
@ThreadSafe
public final class SimpleFieldSearchConstantsWithEmpty implements ClauseInformation
{
    private final String indexField;
    private final ClauseNames jqlClauseNames;
    private final String urlParameter;
    private final String searcherId;
    private final String emptySelectFlag;
    private final String emptyIndexValue;
    private final String fieldId;
    private final Set<Operator> supportedOperators;
    private final JiraDataType supportedType;

    public SimpleFieldSearchConstantsWithEmpty(final String field, final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        this(field, field, field, field, field, field, field, supportedOperators, supportedType);
    }

    public SimpleFieldSearchConstantsWithEmpty(final String indexField, final ClauseNames names,
            final String urlParameter, final String searcherId,
            final String emptySelectFlag, final String emptyIndexValue,
            final String fieldId, final Set<Operator> supportedOperators,
            final JiraDataType supportedType)
    {
        this.supportedType = notNull("supportedType" ,supportedType);
        this.fieldId = notBlank("fieldId", fieldId);
        this.emptySelectFlag = notBlank("emptySelectFlag", emptySelectFlag);
        this.emptyIndexValue = notBlank("emptyIndexValue", emptyIndexValue);
        this.indexField = notBlank("indexField", indexField);
        this.urlParameter = notBlank("urlParameter", urlParameter);
        this.jqlClauseNames = notNull("names", names);
        this.searcherId = notBlank("searcherId", searcherId);
        this.supportedOperators = notNull("supportedOperators", supportedOperators);
    }

    public SimpleFieldSearchConstantsWithEmpty(final String indexField, final String jqlClauseName,
            final String urlParameter, final String searcherId,
            final String emptySelectFlag, final String emptyIndexValue,
            final String fieldId, final Set<Operator> supportedOperators,
            final JiraDataType supportedType)
    {
        this(indexField, new ClauseNames(notBlank("jqlClauseNames", jqlClauseName)),
                urlParameter, searcherId, emptySelectFlag, emptyIndexValue,
                fieldId, supportedOperators, supportedType);
    }

    public String getIndexField()
    {
        return indexField;
    }

    public ClauseNames getJqlClauseNames()
    {
        return jqlClauseNames;
    }

    public String getUrlParameter()
    {
        return urlParameter;
    }

    public String getSearcherId()
    {
        return searcherId;
    }

    public Set<Operator> getSupportedOperators()
    {

        final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager =
                ComponentAccessor.getComponentOfType(IndexedChangeHistoryFieldManager.class);
        if (indexedChangeHistoryFieldManager != null)
        {
           return indexedChangeHistoryFieldManager.getSupportedOperators(this.getJqlClauseNames().getPrimaryName().toLowerCase(), supportedOperators);
        }
        else
        {
           return supportedOperators;
        }
    }

    public JiraDataType getDataType()
    {
        return supportedType;
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

        final SimpleFieldSearchConstantsWithEmpty that = (SimpleFieldSearchConstantsWithEmpty) o;

        if (!emptyIndexValue.equals(that.emptyIndexValue))
        {
            return false;
        }
        if (!emptySelectFlag.equals(that.emptySelectFlag))
        {
            return false;
        }
        if (!fieldId.equals(that.fieldId))
        {
            return false;
        }
        if (!indexField.equals(that.indexField))
        {
            return false;
        }
        if (!jqlClauseNames.equals(that.jqlClauseNames))
        {
            return false;
        }
        if (!searcherId.equals(that.searcherId))
        {
            return false;
        }
        if (!supportedOperators.equals(that.supportedOperators))
        {
            return false;
        }
        if (!supportedType.equals(that.supportedType))
        {
            return false;
        }
        if (!urlParameter.equals(that.urlParameter))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = indexField.hashCode();
        result = 31 * result + jqlClauseNames.hashCode();
        result = 31 * result + urlParameter.hashCode();
        result = 31 * result + searcherId.hashCode();
        result = 31 * result + emptySelectFlag.hashCode();
        result = 31 * result + emptyIndexValue.hashCode();
        result = 31 * result + fieldId.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        result = 31 * result + supportedType.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getEmptySelectFlag()
    {
        return emptySelectFlag;
    }

    public String getEmptyIndexValue()
    {
        return emptyIndexValue;
    }

    public String getFieldId()
    {
        return fieldId;
    }
}