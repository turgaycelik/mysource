package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.query.operator.Operator.EQUALS;

public class VersionStatisticsMapper implements StatisticsMapper, SearchRequestAppender.Factory
{
    private final VersionManager versionManager;
    private final String clauseName;
    private final String documentConstant;
    boolean includeArchived = false;

    public VersionStatisticsMapper(final String clauseName, final String documentConstant, VersionManager versionManager, boolean includeArchived)
    {
        this.clauseName = clauseName;
        this.documentConstant = documentConstant;
        this.versionManager = versionManager;
        this.includeArchived = includeArchived;
    }

    @Override
    public Comparator getComparator()
    {
        return VersionComparator.COMPARATOR;
    }

    @Override
    public boolean isValidValue(Object value)
    {
        if (value == null)
        {
            return true;
        }
        if (!includeArchived)
        {
            return !((Version) value).isArchived();
        }
        else
        {
            return true;
        }
    }

    @Override
    public Object getValueFromLuceneField(String documentValue)
    {
        //JRA-19118: Version custom field may return a null documentValue here.  System version fields will return -1.
        if (StringUtils.isNotBlank(documentValue))
        {
            long versionId = Long.parseLong(documentValue);
            if (versionId > 0)
            {
                return versionManager.getVersion(new Long(versionId));
            }
        }
        return null;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(Object version, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(version, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender getSearchRequestAppender()
    {
        return new VersionSearchRequestAppender(getClauseName());
    }

    protected String getClauseName()
    {
        return clauseName;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    @Override
    public String getDocumentConstant()
    {
        return documentConstant;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final VersionStatisticsMapper that = (VersionStatisticsMapper) o;

        if (includeArchived != that.includeArchived)
        {
            return false;
        }

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);

    }

    @Override
    public int hashCode()
    {
        int result;
        result = (includeArchived ? 1 : 0);
        result = 29 * result + (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
        return result;
    }

    static class VersionSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<Version>, SearchRequestAppender<Version>
    {

        private final String clauseName;

        public VersionSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(Version version, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder
                    .sub()
                    .project(version.getProjectObject().getKey())
                    .and()
                    .addStringCondition(clauseName, EQUALS, version.getName())
                    .endsub();
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Version version, SearchRequest searchRequest)
        {
            return appendAndClause(version, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable versions, SearchRequest searchRequest)
        {
            return appendAndNotClauses(versions, searchRequest, this);
        }
    }
}