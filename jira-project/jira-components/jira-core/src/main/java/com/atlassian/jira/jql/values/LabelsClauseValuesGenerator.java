package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generates completions to be used in the JQL autocomplete.  Possible values will be combined for all matching
 * labels fields. (Custom fields with the same name)
 *
 * @since v4.2
 */
public class LabelsClauseValuesGenerator implements ClauseValuesGenerator
{
    private final LabelManager labelManager;

    public LabelsClauseValuesGenerator(final LabelManager labelManager)
    {
        this.labelManager = labelManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final Set<String> suggestions = new TreeSet<String>();

        final Collection<String> fieldIds = getSearchHandlerManager().getFieldIds(searcher, jqlClauseName);
        for (String fieldId : fieldIds)
        {
            if(fieldId.equals(SystemSearchConstants.forLabels().getFieldId()))
            {
                suggestions.addAll(labelManager.getSuggestedLabels(searcher, null, valuePrefix));
            }
            else
            {
                suggestions.addAll(labelManager.getSuggestedLabels(searcher, null, CustomFieldUtils.getCustomFieldId(fieldId), valuePrefix));
            }
        }
        
        final List<Result> results = new ArrayList<Result>();
        for (String suggestion : suggestions)
        {
            if(results.size() == maxNumResults)
            {
                break;
            }
            results.add(new Result(suggestion));
        }
        return new Results(results);
    }

    //can't be injected due to circular deps
    SearchHandlerManager getSearchHandlerManager()
    {
        return ComponentAccessor.getComponentOfType(SearchHandlerManager.class);
    }
}
