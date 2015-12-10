package com.atlassian.jira.issue.statistics;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.comparator.ConstantsComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.operator.Operator;
import com.opensymphony.util.TextUtils;

import java.util.Comparator;

@PublicSpi
public abstract class AbstractConstantStatisticsMapper implements StatisticsMapper<IssueConstant>
{
    protected final ConstantsManager constantsManager;

    protected AbstractConstantStatisticsMapper(final ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public abstract String getDocumentConstant();

    protected abstract String getConstantType();

    protected abstract String getIssueFieldConstant();

    public Comparator<IssueConstant> getComparator()
    {
        return ConstantsComparator.COMPARATOR;
    }

    public boolean isValidValue(final IssueConstant value)
    {
        return true;
    }

    public IssueConstant getValueFromLuceneField(final String documentValue)
    {
        if (TextUtils.stringSet(documentValue))
        {
            return constantsManager.getConstantObject(getConstantType(), documentValue);
        }

        return null;
    }

    // PRE: searchRequest can not be null
    public SearchRequest getSearchUrlSuffix(final IssueConstant value, final SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            if (value != null)
            {
                final String searchValue = value.getName();
                queryBuilder.where().defaultAnd().addStringCondition(getIssueFieldConstant(), Operator.EQUALS, searchValue);
            }
            else
            {
                //this is only really for 'resolution'.  All other constants should never be null;
                queryBuilder.where().defaultAnd().addEmptyCondition(getIssueFieldConstant());
            }

            return new SearchRequest(queryBuilder.buildQuery());
        }
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final AbstractConstantStatisticsMapper that = (AbstractConstantStatisticsMapper) o;

        final String constantType = getConstantType();
        if (constantType != null ? !constantType.equals(that.getConstantType()) : that.getConstantType() != null)
        {
            return false;
        }

        final String documentConstant = getDocumentConstant();
        return (documentConstant != null ? documentConstant.equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
        result = 29 * result + (getConstantType() != null ? getConstantType().hashCode() : 0);
        return result;
    }
}
