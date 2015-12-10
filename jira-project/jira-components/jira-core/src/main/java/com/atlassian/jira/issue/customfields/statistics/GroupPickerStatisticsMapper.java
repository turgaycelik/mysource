package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.GroupNameComparator;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class GroupPickerStatisticsMapper implements StatisticsMapper<Group>, SearchRequestAppender.Factory<Group>
{
    private final CustomField customField;
    private final ClauseNames clauseNames;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;
    private GroupManager groupManager;

    public GroupPickerStatisticsMapper(CustomField customField, GroupManager groupManager,
            final JiraAuthenticationContext authenticationContext, final CustomFieldInputHelper customFieldInputHelper)
    {
        this.groupManager = groupManager;
        this.authenticationContext = notNull("authenticationContext", authenticationContext);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.customField = notNull("customField", customField);
        this.clauseNames = customField.getClauseNames();
    }

    public String getDocumentConstant()
    {
        return customField.getId();
    }

    public Group getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isBlank(documentValue))
        {
            return null;
        }
        else
        {
            return groupManager.getGroup(documentValue);
        }
    }

    public Comparator<Group> getComparator()
    {
        return new GroupNameComparator();
    }

    protected String getSearchValue(Object value)
    {
        Group group = (Group) value;
        return group.getName();
    }

    public boolean isValidValue(final Group value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return false;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(Group value, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(value, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<Group> getSearchRequestAppender()
    {
        return new GroupSearchRequestAppender(customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), clauseNames.getPrimaryName(), customField.getName()));
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

        final GroupPickerStatisticsMapper that = (GroupPickerStatisticsMapper) o;

        if (!clauseNames.equals(that.clauseNames))
        {
            return false;
        }
        if (!customField.getId().equals(that.customField.getId()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = customField.getId().hashCode();
        result = 31 * result + clauseNames.hashCode();
        return result;
    }

    static class GroupSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<Group>, SearchRequestAppender<Group>
    {
        private final String clauseName;

        GroupSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(Group value, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addStringCondition(clauseName, EQUALS, value.getName());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Group value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends Group> values, SearchRequest searchRequest)
        {
            return appendAndNotClauses(values, searchRequest, this);
        }
    }
}