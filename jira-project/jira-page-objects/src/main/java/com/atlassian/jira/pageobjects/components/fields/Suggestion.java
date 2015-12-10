package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.jira.pageobjects.framework.fields.HasId;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * Suggestion in various dynamic selection compoenents.
 *
 * @since v5.2
 */
public interface Suggestion extends HasId
{
    Suggestion click();

    TimedCondition isActive();

    TimedQuery<String> getText();

}
