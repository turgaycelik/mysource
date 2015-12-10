package com.atlassian.jira.pageobjects.elements;

import com.atlassian.pageobjects.elements.timeout.TimeoutType;

/**
 * Use this annotation to locate a common form title element on JIRA page.
 */
public @interface FormTitle
{
    TimeoutType timeoutType();
}
