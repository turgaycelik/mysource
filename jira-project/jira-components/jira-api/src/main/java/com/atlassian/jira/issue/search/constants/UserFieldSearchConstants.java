package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.search.ClauseNames;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.operator.Operator;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Set;

/**
 * Holds searching constants for user system fields.
 *
 * @since v4.0
 */
@ThreadSafe
public class UserFieldSearchConstants implements ClauseInformation
{
    private final String indexField;
    private final ClauseNames jqlClauseNames;
    private final String searcherId;
    private final String fieldUrlParameter;
    private final String selectUrlParameter;
    private final String currentUserSelectFlag;
    private final String specificUserSelectFlag;
    private final String specificGroupSelectFlag;
    private final String fieldId;
    private final Set<Operator> supportedOperators;

    public UserFieldSearchConstants(final String indexField, final ClauseNames names, final String fieldUrlParameter, final String selectUrlParameter,
            final String searcherId, final String fieldId, final String currentUserSelectFlag,
            final String specificUserSelectFlag, final String specificGroupSelectFlag,
            final Set<Operator> supportedOperators)
    {
        this.fieldId = notBlank("fieldId", fieldId);
        this.currentUserSelectFlag = notNull("currentUserSelectFlag", currentUserSelectFlag);
        this.specificUserSelectFlag = notNull("specificUserSelectFlag", specificUserSelectFlag);
        this.specificGroupSelectFlag = notNull("specificGroupSelectFlag", specificGroupSelectFlag);
        this.fieldUrlParameter = notBlank("fieldUrlParameter", fieldUrlParameter);
        this.selectUrlParameter = notBlank("selectUrlParameter", selectUrlParameter);
        this.indexField = notBlank("indexField", indexField);
        this.jqlClauseNames = notNull("names", names);
        this.searcherId = notBlank("searcherId", searcherId);
        this.supportedOperators = notNull("supportedOperators", supportedOperators);
    }

    public UserFieldSearchConstants(final String indexField, final String jqlClauseName,
            final String fieldUrlParameter, final String selectUrlParameter,
            final String searcherId, final String emptySelectFlag,
            final String fieldId, final Set<Operator> supportedOperators)
    {
        this(indexField, new ClauseNames(notBlank("jqlClauseNames", jqlClauseName)), fieldUrlParameter, selectUrlParameter,
             searcherId, fieldId, DocumentConstants.ISSUE_CURRENT_USER, DocumentConstants.SPECIFIC_USER,
                DocumentConstants.SPECIFIC_GROUP, supportedOperators);
    }

    public String getIndexField()
    {
        return indexField;
    }

    public ClauseNames getJqlClauseNames()
    {
        return jqlClauseNames;
    }

    public String getSearcherId()
    {
        return searcherId;
    }

    public String getFieldUrlParameter()
    {
        return fieldUrlParameter;
    }

    public String getSelectUrlParameter()
    {
        return selectUrlParameter;
    }

    public String getCurrentUserSelectFlag()
    {
        return currentUserSelectFlag;
    }

    public String getSpecificGroupSelectFlag()
    {
        return specificGroupSelectFlag;
    }

    public String getSpecificUserSelectFlag()
    {
        return specificUserSelectFlag;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public Set<Operator> getSupportedOperators()
    {
        final IndexedChangeHistoryFieldManager indexedChangeHistoryFieldManager = ComponentAccessor.getComponentOfType(IndexedChangeHistoryFieldManager.class);
        if (indexedChangeHistoryFieldManager != null)
        {
           return indexedChangeHistoryFieldManager.getSupportedOperators(this.getFieldId(), supportedOperators);
        }
        else
        {
           return supportedOperators;
        }
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.USER;
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

        final UserFieldSearchConstants that = (UserFieldSearchConstants) o;

        if (currentUserSelectFlag != null ? !currentUserSelectFlag.equals(that.currentUserSelectFlag) : that.currentUserSelectFlag != null)
        {
            return false;
        }
        if (fieldId != null ? !fieldId.equals(that.fieldId) : that.fieldId != null)
        {
            return false;
        }
        if (fieldUrlParameter != null ? !fieldUrlParameter.equals(that.fieldUrlParameter) : that.fieldUrlParameter != null)
        {
            return false;
        }
        if (indexField != null ? !indexField.equals(that.indexField) : that.indexField != null)
        {
            return false;
        }
        if (jqlClauseNames != null ? !jqlClauseNames.equals(that.jqlClauseNames) : that.jqlClauseNames != null)
        {
            return false;
        }
        if (searcherId != null ? !searcherId.equals(that.searcherId) : that.searcherId != null)
        {
            return false;
        }
        if (selectUrlParameter != null ? !selectUrlParameter.equals(that.selectUrlParameter) : that.selectUrlParameter != null)
        {
            return false;
        }
        if (specificGroupSelectFlag != null ? !specificGroupSelectFlag.equals(that.specificGroupSelectFlag) : that.specificGroupSelectFlag != null)
        {
            return false;
        }
        if (specificUserSelectFlag != null ? !specificUserSelectFlag.equals(that.specificUserSelectFlag) : that.specificUserSelectFlag != null)
        {
            return false;
        }
        if (supportedOperators != null ? !supportedOperators.equals(that.supportedOperators) : that.supportedOperators != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}