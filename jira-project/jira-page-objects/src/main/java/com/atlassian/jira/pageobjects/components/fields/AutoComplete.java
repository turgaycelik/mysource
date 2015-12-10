package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import java.util.List;

/**
 * An interface for Autocomplete pickers. E.G dot, multiselect, singleselect, userpickers etc
 *
 * @since v4.4
 */
public interface AutoComplete
{
    /**
     * Gets a list of suggestions visible in dropdown
     *
     * @return suggestions list
     */
    public List<MultiSelectSuggestion> getSuggestions();

    /**
     * Gets a list of suggestions visible in dropdown, as a timed query
     *
     * @return suggestions list query
     */
    public TimedQuery<Iterable<MultiSelectSuggestion>> getTimedSuggestions();

    /**
     * Clear string in the textfield.
     */
    public AutoComplete clearQuery();

    /**
     * Types string into textfield and waits for input to be evaluated
     *
     * @param query - input to be typed into text field
     * @return instance
     */
    public AutoComplete query(final String query);

    /**
     * Selects specified suggestion using mouse click
     *
     * @param suggestion suggestion to be clicked
     * @return instance
     */
    public AutoComplete acceptUsingMouse(final MultiSelectSuggestion suggestion);

    /**
     * Presses enter on specified suggestion
     *
     * @param suggestion - suggestion to be navigated to
     * @return instance
     */
    public AutoComplete acceptUsingKeyboard(final MultiSelectSuggestion suggestion);

    /**
     * Navigates selection down using DOWN key. The number of times down is pressed is specified by steps.
     *
     * @param steps - Number of times down key is pressed
     * @return instance
     */
    public AutoComplete down(final int steps);

    /**
     * Navigates selection down using UP key. The number of times up is pressed is specified by steps.
     *
     * @param steps - Number of times up key is pressed
     * @return instance
     */
    public AutoComplete up(final int steps);

    /**
     * If there is an overlabel, will return the PageElement
     *
     * @return Label element
     */
    public PageElement getLabel();

    /**
     * Gets active suggestion
     *
     * @return active suggestion
     */
    public MultiSelectSuggestion getActiveSuggestion();

    /**
     * Gets active suggestion as a timed query
     *
     * @return active suggestion
     */
    public TimedQuery<MultiSelectSuggestion> getTimedActiveSuggestion();

    /**
     * Returns if the control is present on page
     *
     * @return if present
     */
    public boolean isPresent();

    /**
     * Returns true if the control is present on page
     *
     * @return <code>true</code> if present
     */
    public TimedQuery<Boolean> timedIsPresent();
}
