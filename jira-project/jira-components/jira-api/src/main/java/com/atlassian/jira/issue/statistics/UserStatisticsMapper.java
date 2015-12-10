package com.atlassian.jira.issue.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.base.Function;

import java.util.Comparator;
import java.util.Locale;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;

public class UserStatisticsMapper implements StatisticsMapper<User>, SearchRequestAppender.Factory<User>
{
    private final String clauseName;
    private final String indexedField;
    private final UserResolver userResolver;
    protected final JiraAuthenticationContext jiraAuthenticationContext;

    public UserStatisticsMapper(final UserFieldSearchConstantsWithEmpty searchConstants, final UserManager userManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this(searchConstants.getJqlClauseNames().getPrimaryName(), searchConstants.getEmptyIndexValue(), searchConstants.getIndexField(),
                userManager, jiraAuthenticationContext);
    }

    public UserStatisticsMapper(final String clauseName, final String emptyIndexValue, final String indexedField, final UserManager userManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.clauseName = notBlank("clauseName", clauseName);
        this.indexedField = notBlank("indexedField", indexedField);
        userResolver = new UserManagerResolver(userManager, emptyIndexValue);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public Comparator<User> getComparator()
    {
        return new UserCachingComparator(getLocale());
    }

    public boolean isValidValue(final User value)
    {
        return true;
    }

    public User getValueFromLuceneField(final String documentValue)
    {
        return userResolver.apply(documentValue);
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    Locale getLocale()
    {
        return jiraAuthenticationContext.getLocale();
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(final User user, final SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(user, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<User> getSearchRequestAppender()
    {
        return new UserSearchRequestAppender(getClauseName());
    }

    protected String getClauseName()
    {
        return clauseName;
    }

    public String getDocumentConstant()
    {
        return indexedField;
    }

    TerminalClause getUserClause(final String name)
    {
        return new TerminalClauseImpl(clauseName, Operator.EQUALS, name);
    }

    TerminalClause getEmptyUserClause()
    {
        return new TerminalClauseImpl(clauseName, Operator.IS, EmptyOperand.EMPTY);
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

        final UserStatisticsMapper that = (UserStatisticsMapper) o;

        if (!clauseName.equals(that.clauseName))
        {
            return false;
        }
        if (!indexedField.equals(that.indexedField))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = clauseName.hashCode();
        result = 31 * result + indexedField.hashCode();
        return result;
    }

    /**
     * Get a User given a user name.
     */
    interface UserResolver extends Function<String, User>
    {
    }

    static class UserManagerResolver implements UserResolver
    {
        private final UserManager userManager;
        private final String emptyIndexValue;

        public UserManagerResolver(final UserManager userManager, final String emptyIndexValue)
        {
            this.userManager = userManager;
            this.emptyIndexValue = emptyIndexValue;
        }

        public User apply(final String documentValue)
        {
            if ((emptyIndexValue != null) && emptyIndexValue.equals(documentValue))
            {
                return null;
            }
            if (documentValue == null)
            {
                return null;
            }
            return ApplicationUsers.toDirectoryUser(userManager.getUserByKey(documentValue));
        }
    }

    static class UserSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<User>, SearchRequestAppender<User>
    {
        private final String clauseName;

        public UserSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(User value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends User> values, SearchRequest searchRequest)
        {
            return appendAndNotClauses(values, searchRequest, this);
        }

        @Override
        public void appendNonNullItem(User user, JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addStringCondition(clauseName, (user).getName());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }
    }
}