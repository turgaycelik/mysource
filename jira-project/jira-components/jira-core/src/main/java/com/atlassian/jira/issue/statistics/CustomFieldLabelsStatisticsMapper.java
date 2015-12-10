package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;

/**
 * @since v4.2
 */
public class CustomFieldLabelsStatisticsMapper extends LabelsStatisticsMapper implements SearchRequestAppender.Factory
{
    private CustomField customField;
    private CustomFieldInputHelper customFieldInputHelper;
    private final JiraAuthenticationContext authenticationContext;

    public CustomFieldLabelsStatisticsMapper(CustomField customField, final CustomFieldInputHelper customFieldInputHelper,
            final JiraAuthenticationContext authenticationContext, final boolean includeEmpty)
    {
        super(JqlCustomFieldId.toString(customField.getIdAsLong()), customField.getId(), includeEmpty);
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
        this.authenticationContext = authenticationContext;
    }

    @Override
    protected String getClauseName()
    {
        return customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), customField.getClauseNames().getPrimaryName(), customField.getName());
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Deprecated
    @Override
    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(value, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender getSearchRequestAppender()
    {
        return new CustomFieldLabelsSearchRequestAppender(customField, getClauseName());
    }

    static class CustomFieldLabelsSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<Object>, SearchRequestAppender
    {

        private final CustomField customField;
        private final String clauseName;

        public CustomFieldLabelsSearchRequestAppender(CustomField customField, String clauseName)
        {
            this.customField = Assertions.notNull(customField);
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(Object value, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.customField(customField.getIdAsLong()).eq(value.toString());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Object value, SearchRequest searchRequest)
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