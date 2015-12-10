package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.issue.statistics.ValueStatisticMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.operator.Operator;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

@Internal
public class SelectStatisticsMapper implements ValueStatisticMapper<Option>, SearchRequestAppender.Factory<Option>
{
    public static final String RAW_VALUE_SUFFIX = "_raw";

    private final CustomField customField;
    private final ClauseNames clauseNames;
    private final SelectConverter selectConverter;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;

    public SelectStatisticsMapper(CustomField customField,
            SelectConverter selectConverter, final JiraAuthenticationContext authenticationContext,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        this.authenticationContext = authenticationContext;
        this.customFieldInputHelper = customFieldInputHelper;
        this.customField = notNull("customField", customField);
        this.clauseNames = customField.getClauseNames();
        this.selectConverter = notNull("selectConverter", selectConverter);
    }

    protected String getSearchValue(Option value)
    {
        return selectConverter.getString(value);
    }

    @Override
    public String getDocumentConstant()
    {
        return customField.getId() + RAW_VALUE_SUFFIX;
    }

    @Override
    public Option getValueFromLuceneField(String documentValue)
    {
        return selectConverter.getObject(documentValue);
    }

    @Override
    public Comparator<Option> getComparator()
    {
        return new OptionComparator();
    }

    @Override
    public Comparator<Option> getValueComparator()
    {
        return new OptionValueComparator();
    }

    @Override
    public boolean isValidValue(final Option value)
    {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return false;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(final Option option, final SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(option, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<Option> getSearchRequestAppender()
    {
        return new SelectOptionSearchRequestAppender(customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), clauseNames.getPrimaryName(), customField.getName()));
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

        final SelectStatisticsMapper that = (SelectStatisticsMapper) o;

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

    static class SelectOptionSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<Option>, SearchRequestAppender<Option>
    {
        final String clauseName;

        public SelectOptionSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(Option value, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addStringCondition(clauseName, Operator.EQUALS, value.getValue());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Option value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
        {
            return appendAndNotClauses((Iterable<? extends Option>) values, searchRequest, this);
        }
    }
}
