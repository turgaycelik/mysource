package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.components.fields.SuggestionGroup;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents an Issue Picker.
 *
 * @since v5.2
 */
public class IssuePicker extends MultiSelect
{

    public IssuePicker(final String id)
    {
        super(id);
    }


    public PageElement getQueryInput()
    {
        return textArea;
    }

    public TimedCondition hasHistorySearchSuggestions()
    {
        return hasSuggestionGroup("history-search");
    }

    public SuggestionGroup getHistorySearchSuggestions()
    {
        return getSuggestionGroup("history-search");
    }

    public TimedCondition hasCurrentSearchSuggestions()
    {
        return hasSuggestionGroup("current-search");
    }

    public SuggestionGroup getCurrentSearchSuggestions()
    {
        return getSuggestionGroup("current-search");
    }

    public TimedCondition hasUserInputtedOptionSuggestions()
    {
        return hasSuggestionGroup("user-inputted-option");
    }

    public SuggestionGroup getUserInputtedOptionSuggestions()
    {
        return getSuggestionGroup("user-inputted-option");
    }

}
