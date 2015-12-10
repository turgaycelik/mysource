package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.dev.reference.plugin.components.ReferenceComponent;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Simple web action that displays the result of calling a method on {@link ReferenceComponent}
 *
 * @since v4.3
 */
public class ReferenceComponentAction extends JiraWebActionSupport
{
    final ReferenceComponent referenceComponent;

    public ReferenceComponentAction(final ReferenceComponent referenceComponent)
    {
        this.referenceComponent = referenceComponent;
    }

    public String getMessage()
    {
        return referenceComponent.getMessage();
    }
}
