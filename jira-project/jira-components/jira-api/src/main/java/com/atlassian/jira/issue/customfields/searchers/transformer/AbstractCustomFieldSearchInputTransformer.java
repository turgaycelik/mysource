package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An abstract implementation for a search input transformer for custom fields. Assumes the custom field params contain
 * a single mapping from field to value.
 *
 * @since v4.0
 */
@PublicSpi
abstract public class AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(AbstractCustomFieldSearchInputTransformer.class);

    private final CustomField field;
    private final String urlParameterName;
    private final CustomFieldInputHelper customFieldInputHelper;

    public AbstractCustomFieldSearchInputTransformer(CustomField field, String urlParameterName, final CustomFieldInputHelper customFieldInputHelper)
    {
        this.field = field;
        this.urlParameterName = urlParameterName;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
    }

    /**
     * Note: only provide a sensible implementation of this if you intend on using {@link #getSearchClause(User, com.atlassian.jira.issue.transport.FieldValuesHolder)}
     * as implemented here. If you are overriding {@link #getSearchClause(User, com.atlassian.jira.issue.transport.FieldValuesHolder)}
     * then you do not have to implement this method sensibly.
     *
     * @param user the user performing the search
     * @param customFieldParams the custom field params
     * @return the clause that represents the params
     */
    protected abstract Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams);

    /**
     * Gets CustomField search parameters from the given JQL Query.
     *
     * <p> it may return null, indicating no search parameters for this searcher were specified, or the query will not
     * fit into the "simple navigator".
     *
     * @param user User
     * @param query JQL query
     * @param searchContext contains the projects and issue types that the search is restricted to 
     * @return CustomField search parameters from the given JQL Query.
     */
    protected abstract CustomFieldParams getParamsFromSearchRequest(User user, Query query, SearchContext searchContext);

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        if (fieldValuesHolder.containsKey(urlParameterName))
        {
            CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(urlParameterName);
            if (customFieldParams != null && !customFieldParams.isEmpty())
            {
                return getClauseFromParams(user, customFieldParams);
            }
        }
        return null;
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        getCustomField().populateFromParams(fieldValuesHolder, actionParams.getKeysAndValues());
    }

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        final CustomFieldParams customFieldParams = getParamsFromSearchRequest(user, query, searchContext);
        if (customFieldParams != null && !customFieldParams.isEmpty())
        {
            fieldValuesHolder.put(urlParameterName, customFieldParams);
        }
    }

    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        if (fieldValuesHolder != null && fieldValuesHolder.containsKey(urlParameterName))
        {
            CustomField customField = getCustomField();
            final FieldConfig config = customField.getReleventConfig(searchContext);
            if (config != null)
            {
                final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(urlParameterName);
                customField.getCustomFieldType().validateFromParams(customFieldParams, errors, config);
            }
            else
            {
                log.warn("Searcher " + urlParameterName + " (" + ClassUtils.getShortClassName(getClass()) + ") tried to search with context it does not exist for. The search context is " + searchContext);
            }
        }
    }

    protected String getClauseName(User user, ClauseNames clauseNames)
    {
        return customFieldInputHelper.getUniqueClauseName(user, clauseNames.getPrimaryName(), field.getUntranslatedName());
    }

    ///CLOVER:OFF
    protected CustomField getCustomField()
    {
        return field;
    }
    ///CLOVER:ON
}
