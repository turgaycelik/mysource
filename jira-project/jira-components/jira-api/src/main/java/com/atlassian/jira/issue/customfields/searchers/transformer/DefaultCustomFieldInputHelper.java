package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.InjectableComponent;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link CustomFieldInputHelper}
 *
 * @since v4.0
 */
@InjectableComponent
public class DefaultCustomFieldInputHelper implements CustomFieldInputHelper
{
    private final SearchHandlerManager searchHandlerManager;

    public DefaultCustomFieldInputHelper(final SearchHandlerManager searchHandlerManager)
    {
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
    }

    public String getUniqueClauseName(final User user, final String primaryName, final String fieldName)
    {
        // we must check that the name of the field is not something that would cause it to not be registered in the
        // SearchHandlerManager, for this would mean that the name is potentially not unique
        if (!SystemSearchConstants.isSystemName(fieldName))
        {
            if (!JqlCustomFieldId.isJqlCustomFieldId(fieldName))
            {
                if (searchHandlerManager.getClauseHandler(user, fieldName).size() == 1)
                {
                    return fieldName;
                }
            }
        }
        return primaryName;
    }
}
