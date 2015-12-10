package com.atlassian.jira.dev.reference.dependent.plugin.actions;

import com.atlassian.jira.dev.reference.plugin.components.ReferencePublicComponent;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Simple web action that displays the result of calling a method on {@link ReferencePublicComponent}
 *
 * @since v4.3
 */
public class ReferenceComponentImportAction extends JiraWebActionSupport
{
    final ReferencePublicComponent referencePublicComponent;

    public ReferenceComponentImportAction(final ReferencePublicComponent referencePublicComponent)
    {
        this.referencePublicComponent = referencePublicComponent;
    }

    public String getMessage()
    {
        return referencePublicComponent.getMessage();
    }
}
