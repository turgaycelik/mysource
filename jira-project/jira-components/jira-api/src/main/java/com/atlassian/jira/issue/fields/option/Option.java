package com.atlassian.jira.issue.fields.option;

import com.atlassian.annotations.PublicApi;

import java.util.List;

/**
 * An option class to wrap around other objects for display in select lists
 */
@PublicApi
public interface Option
{
    String getId();
    String getName();
    String getDescription();

    String getImagePath();

    /**
     * Returns the HTML-encoded image path for this Option.
     *
     * @return an HTML-encoded image path
     * @see #getImagePath()
     */
    String getImagePathHtml();

    String getCssClass();

    /**
     * Returns a list of dependent child options. Returns empty list if no children
     * @return  List of {@link Option} objects. (empty list if no children)
     */
    List getChildOptions();
}
