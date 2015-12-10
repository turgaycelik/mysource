package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.TerminalClauseImpl;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.query.operator.Operator.EQUALS;

public class ProjectStatisticsMapper implements StatisticsMapper, SearchRequestAppender.Factory<GenericValue>
{
    private final ProjectManager projectManager;
    private final String clauseName;
    private final String documentConstant;

    public ProjectStatisticsMapper(ProjectManager projectManager)
    {
        this(projectManager, SystemSearchConstants.forProject());
    }

    public ProjectStatisticsMapper(ProjectManager projectManager, SimpleFieldSearchConstants searchConstants)
    {
        this(projectManager, searchConstants.getJqlClauseNames().getPrimaryName(), searchConstants.getIndexField());
    }

    public ProjectStatisticsMapper(ProjectManager projectManager, String clauseName, String documentConstant)
    {
        this.projectManager = projectManager;
        this.clauseName = clauseName;
        this.documentConstant = documentConstant;
    }

    @Override
    public String getDocumentConstant()
    {
        return documentConstant;
    }

    @Override
    public Object getValueFromLuceneField(String documentValue)
    {
        //JRA-19121: Project custom field may return a null documentValue here.  System version fields will return -1.
        if (StringUtils.isNotBlank(documentValue))
        {
            long projectId = Long.parseLong(documentValue);
            if (projectId > 0)
            {
                return projectManager.getProject(projectId);
            }
        }
        return null;
    }

    @Override
    public Comparator getComparator()
    {
        return OfBizComparators.NAME_COMPARATOR;
    }

    @Override
    public boolean isValidValue(Object value)
    {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Deprecated
    @Override
    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause((GenericValue) value, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<GenericValue> getSearchRequestAppender()
    {
        return new ProjectSearchRequestAppender(projectManager, getClauseName());
    }

    protected String getClauseName()
    {
        return clauseName;
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

        final ProjectStatisticsMapper that = (ProjectStatisticsMapper) o;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    @Override
    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }

    static class ProjectSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<GenericValue>, SearchRequestAppender<GenericValue>
    {

        private final ProjectManager projectManager;
        private final String clauseName;

        public ProjectSearchRequestAppender(ProjectManager projectManager, String clauseName)
        {
            this.projectManager = Assertions.notNull(projectManager);
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(GenericValue value, JqlClauseBuilder clauseBuilder)
        {
            final Long projectId = value.getLong("id");
            final Project project = projectManager.getProjectObj(projectId);
            clauseBuilder.addClause(new TerminalClauseImpl(clauseName, EQUALS, project.getKey()));
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(GenericValue value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
        {
            return appendAndNotClauses(values, searchRequest, this);
        }
    }
}