package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.query.Query;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for project custom fields.
 *
 * @since v4.0
 */
public class ExactNumberCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    public ExactNumberCustomFieldSearchInputTransformer(CustomField field, ClauseNames clauseNames, String urlParameterName,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return convertForNavigator(query).fitsNavigator();
    }

}