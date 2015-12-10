package com.atlassian.jira.pageobjects.framework.fields;

import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * A page object with ID.
 *
 * @since v5.2
 */
public interface HasId
{

    TimedQuery<String> getId();
}
