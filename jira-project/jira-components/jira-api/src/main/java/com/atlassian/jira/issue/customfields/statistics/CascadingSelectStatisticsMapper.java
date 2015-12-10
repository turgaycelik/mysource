package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
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
public class CascadingSelectStatisticsMapper implements ValueStatisticMapper<CascadingOption>, SearchRequestAppender.Factory<CascadingOption>
{
    public static final String SUB_VALUE_SUFFIX = "_combined";
    public static final String PARENT_AND_CHILD_INDEX_SEPARATOR = "|";

    private final CustomField customField;
    private final ClauseNames clauseNames;
    private final SelectConverter selectConverter;
    private JiraAuthenticationContext authenticationContext;
    private CustomFieldInputHelper customFieldInputHelper;

    public CascadingSelectStatisticsMapper(CustomField customField,
            SelectConverter selectConverter, final JiraAuthenticationContext authenticationContext,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        this.authenticationContext = authenticationContext;
        this.customFieldInputHelper = customFieldInputHelper;
        this.customField = notNull("customField", customField);
        this.clauseNames = customField.getClauseNames();
        this.selectConverter = notNull("selectConverter", selectConverter);
    }

    @Override
    public String getDocumentConstant()
    {
        return customField.getId() + SUB_VALUE_SUFFIX;
    }

    @Override
    public CascadingOption getValueFromLuceneField(final String documentValue)
    {
        if (documentValue == null || "".equals(documentValue))
        {
            return null;
        }
        // The separator is | so we need to escape it when turning it into a regex
        String[] valueSections = documentValue.split("\\" + PARENT_AND_CHILD_INDEX_SEPARATOR);
        switch (valueSections.length)
        {
            case 0:
                return null;
            case 1:
                Option onlyParent = selectConverter.getObject(valueSections[0]);
                return new CascadingOption(onlyParent, null);
            case 2:
                Option parent = selectConverter.getObject(valueSections[0]);
                Option child = selectConverter.getObject(valueSections[1]);
                return new CascadingOption(parent, child);
            default:
                throw new FieldValidationException("Option Id '" + documentValue + "' is not a valid cascading select pair.");
        }
    }

    @Override
    public Comparator<CascadingOption> getComparator()
    {
        return new CascadingOptionComparator();
    }

    @Override
    public SearchRequestAppender<CascadingOption> getSearchRequestAppender()
    {
        return new SelectCascadingOptionSearchRequestAppender(customFieldInputHelper.getUniqueClauseName(authenticationContext.getLoggedInUser(), clauseNames.getPrimaryName(), customField.getName()));
    }

    @Override
    public Comparator<CascadingOption> getValueComparator()
    {
       return new CascadingOptionValueComparator();
    }

    @Override
    public boolean isValidValue(final CascadingOption value)
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
    public SearchRequest getSearchUrlSuffix(final CascadingOption option, final SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(option, searchRequest);
    }

    @Override
    public int hashCode()
    {
        int result = customField.getId().hashCode();
        result = 31 * result + clauseNames.hashCode();
        return result;
    }

    static class SelectCascadingOptionSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<CascadingOption>, SearchRequestAppender<CascadingOption>
    {
        final String clauseName;

        public SelectCascadingOptionSearchRequestAppender(String clauseName)
        {
            this.clauseName = Assertions.notNull(clauseName);
        }

        @Override
        public void appendNonNullItem(CascadingOption value, JqlClauseBuilder clauseBuilder)
        {
            // Use Values, not IDs, because at this point each slice of the pie represents
            // a combination of all options with this value.
            if (value.getChild() == null)
            {
                clauseBuilder.addFunctionCondition(clauseName, Operator.IN, "cascadeOption",
                        new String [] {value.getParent().getValue(), "none"});
            }
            else
            {
                clauseBuilder.addFunctionCondition(clauseName, Operator.IN, "cascadeOption",
                        new String [] {value.getParent().getValue().toString(),
                                       value.getChild().getValue().toString() });
            }
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(CascadingOption value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
        {
            return appendAndNotClauses((Iterable<? extends CascadingOption>) values, searchRequest, this);
        }
    }
}
