package com.atlassian.jira.event.fields.layout;

import com.atlassian.jira.util.NamedWithDescription;
import com.atlassian.jira.util.NamedWithId;

/**
 *
 * @since v6.2
 */
public interface FieldLayoutSchemeDetails extends NamedWithDescription, NamedWithId
{
    Long getId();
}
