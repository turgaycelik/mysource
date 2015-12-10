package com.atlassian.jira.plugin.assignee.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.project.DefaultAssigneeException;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * The default implementation of {@link AssigneeResolver} that is shipped with JIRA.
 * Bases the 'Automatic' assignee reoslution on Project and Component values.
 */
public class DefaultAssigneeResolver implements AssigneeResolver
{
    private static final Logger log = Logger.getLogger(DefaultAssigneeResolver.class);
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultAssigneeResolver(ProjectManager projectManager, JiraAuthenticationContext authenticationContext)
    {
        this.projectManager = projectManager;
        this.authenticationContext = authenticationContext;
    }

    public ErrorCollection validateDefaultAssignee(Issue issue, Map fieldValuesHolder)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        try
        {
            getDefaultAssigneeObject(issue, fieldValuesHolder);
        }
        catch (DefaultAssigneeException ex)
        {
            // log detailed message in English
            log.warn(ex.getMessage());
            // Display localised error message.
            errorCollection.addError(IssueFieldConstants.ASSIGNEE, authenticationContext.getI18nHelper().getText("assign.error.unassignabledefault"), Reason.VALIDATION_FAILED);
        }

        return errorCollection;
    }

    /**
     * Resolves the 'Automatic' assignee based on Project and Component values of the issue
     */
    @Override
    public User getDefaultAssignee(Issue issue, Map fieldValuesHolder)
    {
       return getDefaultAssigneeObject(issue, fieldValuesHolder);
    }

    @Override
    public User getDefaultAssigneeObject(Issue issue, Map fieldValuesHolder)
    {
        // see if new components are being set
        Collection<ProjectComponent> components = getComponentsSystemField().getComponentsFromParams(fieldValuesHolder);
        if (components == null)
        {
            // components not set in fieldValuesHolder - use existing value (eg this is used when doing "assign issue" operation)
            components = issue.getComponentObjects();
        }

        return projectManager.getDefaultAssignee(issue.getProjectObject(), components);
    }

    private ComponentsSystemField getComponentsSystemField()
    {
        return (ComponentsSystemField) ComponentAccessor.getFieldManager().getField(IssueFieldConstants.COMPONENTS);
    }
}
