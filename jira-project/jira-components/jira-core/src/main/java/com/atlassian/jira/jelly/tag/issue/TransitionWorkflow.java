package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.JellyUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransitionWorkflow extends JiraDynaBeanTagSupport implements CustomFieldValuesAwareTag
{
    // Tag's attribtes
    private static final String KEY_USER = "user";
    private static final String KEY_ISSUE_KEY = "key";
    private static final String KEY_WORKFLOW_ACTION = "workflowAction";
    // Comment attributes
    private static final String KEY_COMMENT = WorkflowTransitionUtil.FIELD_COMMENT;
    private static final String KEY_GROUP_COMMENT_LEVEL = WorkflowTransitionUtil.FIELD_COMMENT_GROUP_LEVEL;
    private static final String KEY_ROLE_COMMENT_LEVEL = WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL;

    public static final Set<String> attributes;

    static
    {
        Set<String> tmpAttributes = new HashSet<String>();
        tmpAttributes.add(KEY_USER);
        tmpAttributes.add(KEY_ISSUE_KEY);
        tmpAttributes.add(KEY_WORKFLOW_ACTION);
        tmpAttributes.add(KEY_COMMENT);
        tmpAttributes.add(KEY_GROUP_COMMENT_LEVEL);
        tmpAttributes.add(KEY_ROLE_COMMENT_LEVEL);
        attributes = Collections.unmodifiableSet(tmpAttributes);
    }

    private final JiraAuthenticationContext authenticationContext;
    private final IssueManager issueManager;
    private final WorkflowManager workflowManager;
    private final FieldManager fieldManager;
    private final IssueFactory issueFactory;
    private final IssueWorkflowManager issueWorkflowManager;

    public TransitionWorkflow(JiraAuthenticationContext authenticationContext, IssueManager issueManager,
            WorkflowManager workflowManager, FieldManager fieldManager, IssueFactory issueFactory,
            IssueWorkflowManager issueWorkflowManager)
    {
        this.authenticationContext = authenticationContext;
        this.issueManager = issueManager;
        this.workflowManager = workflowManager;
        this.fieldManager = fieldManager;
        this.issueFactory = issueFactory;
        this.issueWorkflowManager = issueWorkflowManager;
    }

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        if (getBody() != null)
        {
            getBody().run(getContext(), xmlOutput);
        }

        validateAttributes();

        User user = getUser();

        // If we have the properties we need to check if we can identify the workflow transition
        MutableIssue issue = getIssue();

        int actionId = getActionId(issue).getId();

        // Remember the current user
        Map requestCacheContents = JiraAuthenticationContextImpl.getRequestCache();
        User previousUser = authenticationContext.getLoggedInUser();

        try
        {
            // Execute as specified user
            authenticationContext.setLoggedInUser(user);
            JiraAuthenticationContextImpl.clearRequestCache();

            //WorkflowTransitionUtil needs to be loaded _after_ the authenticationContext.setUser(user) call
            final WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue(issue);
            workflowTransitionUtil.setAction(actionId);

            Map params = getTransitionParameters(workflowTransitionUtil, issue);
            workflowTransitionUtil.setParams(params);

            // Now we need to process and validate all the fields that can be set during the transition
            // Validate input
            JellyUtils.processErrorCollection(workflowTransitionUtil.validate());

            // Progress the issue through workflow
            JellyUtils.processErrorCollection(workflowTransitionUtil.progress());
        }
        finally
        {
            // Restore the user
            authenticationContext.setLoggedInUser(previousUser);
            JiraAuthenticationContextImpl.clearRequestCache();
            if (requestCacheContents != null)
            {
                // Restore all the old requestCacheContents
                JiraAuthenticationContextImpl.getRequestCache().putAll(requestCacheContents);
            }

            // Restore properties
            restoreProperties();
        }

    }

    private ActionDescriptor getActionId(Issue issue) throws JellyTagException
    {
        try
        {
            JiraWorkflow workflow = workflowManager.getWorkflow(issue.getGenericValue());

            try
            {
                int actionId = Integer.parseInt(getWorkflowActionProperty());
                ActionDescriptor action = workflow.getDescriptor().getAction(actionId);
                if (action == null)
                {
                    throw new JellyTagException("Invalid action id '" + getWorkflowActionProperty() + "'.");
                }
                else
                {
                    validateActionForIssuesState(issue, action);
                }
                return action;
            }
            catch (NumberFormatException e)
            {
                return getActionByName(workflow, issue);
            }
        }
        catch (WorkflowException e)
        {
            throw new JellyTagException("Error occurred while retrieving workflow for issue with key '" + getKey() + "'", e);
        }
    }

    private MutableIssue getIssue() throws JellyTagException
    {
        MutableIssue issue;
        try
        {
            GenericValue issueGV = issueManager.getIssue(getKey());

            if (issueGV == null)
            {
                throw new JellyTagException("Cannot retrieve issue with key '" + getKey() + "'");
            }

            issue = getIssueObject(issueGV);
        }
        catch (GenericEntityException e)
        {
            throw new JellyTagException("Error occurred while retrieving issue with key '" + getKey() + "'", e);
        }
        return issue;
    }

    private void validateActionForIssuesState(Issue issue, ActionDescriptor action) throws JellyTagException
    {
        if (!issueWorkflowManager.isValidAction(issue, action.getId(), getApplicationUser()))
        {
            throw new JellyTagException(getFoundButNotValidMessage(String.valueOf(action.getId()), issue));
        }
    }

    private String getFoundButNotValidMessage(String actionName, Issue issue)
    {
        return "Found workflow transition with name/id '" + actionName
                + "' but that is not a valid workflow transition for the current state of issue '"
                + issue.getKey() + "'.";
    }

    public MutableIssue getIssueObject(GenericValue issueGV)
    {
        return issueFactory.getIssue(issueGV);
    }

    private Map getTransitionParameters(WorkflowTransitionUtil workflowTransitionUtil, Issue issue)
            throws JellyTagException
    {
        final Map<String, Object> workflowTransitionParams = new HashMap<String, Object>();

        if (paramSpecified(KEY_COMMENT))
        {
            workflowTransitionParams.put(WorkflowTransitionUtil.FIELD_COMMENT, getComment());

            if (paramSpecified(KEY_GROUP_COMMENT_LEVEL))
            {
                workflowTransitionParams.put(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL, getGroupCommentLevel());
            }

            if (paramSpecified(KEY_ROLE_COMMENT_LEVEL))
            {
                workflowTransitionParams.put(WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL, getProjectRoleId(getRoleCommentLevel(), getUser(), new SimpleErrorCollection()));
            }
        }

        if (workflowTransitionUtil.hasScreen())
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : workflowTransitionUtil.getFieldScreenRenderer().getFieldScreenRenderTabs())
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
                {
                    if (fieldScreenRenderLayoutItem.isShow(issue))
                    {
                        OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                        //JRA-16915: first popuplate the fieldvalues holder used for the transition from the value stored in the
                        //issue. This is so that existing values don't get lost.
                        orderableField.populateFromIssue(workflowTransitionParams, issue);

                        //Only override the fields value if something was submitted in the jelly request for this field.
                        //Using the fieldId as the key into the actionParam map.  This is safe, since the id is always
                        //used to key the field in the parameter map (see implementations of
                        // {@link com.atlassian.jira.issue.fields.AbstractOrderableField#getRelevantParams(Map params)}).
                        if (getProperties().containsKey(orderableField.getId()))
                        {
                            if (fieldManager.isCustomField(orderableField))
                            {
                                final Map<String, String[]> actionParams = new HashMap<String, String[]>();
                                for (final Object o : getProperties().keySet())
                                {
                                    final String key = (String) o;
                                    if (key.startsWith(orderableField.getId()))
                                    {
                                        @SuppressWarnings ("unchecked")
                                        final List<String> values = (List<String>) getProperties().get(key);
                                        actionParams.put(key, values.toArray(new String[values.size()]));
                                    }
                                }
                                orderableField.populateFromParams(workflowTransitionParams, actionParams);
                            }
                            else
                            {
                                try
                                {
                                    orderableField.populateParamsFromString(workflowTransitionParams, (String) getProperties().get(orderableField.getId()), issue);
                                }
                                catch (FieldValidationException e)
                                {
                                    throw new JellyTagException(e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        }

        return workflowTransitionParams;
    }

    private User getUser()
    {
        String username;
        if (paramSpecified(KEY_USER))
        {
            username = getUsername();
        }
        else
        {
            // Get the user from context
            username = (String) getContext().getVariable(JellyTagConstants.USERNAME);
        }

        User user;
        if (TextUtils.stringSet(username))
        {
            user = UserUtils.getUser(username);
        }
        else
        {
            user = null;
        }

        return user;
    }

    private ApplicationUser getApplicationUser() {
        return ApplicationUsers.from(getUser());
    }

    private String getProjectRoleId(String roleLevel, User user, ErrorCollection errorCollection) throws JellyTagException
    {
        // Find the role for the comment if this is set.
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
        Long roleLevelId = null;
        if (roleLevel != null)
        {
            try
            {
                roleLevelId = new Long(roleLevel);
            }
            catch (NumberFormatException nfe)
            {
                // We will just fall back to resolving it by name
            }

            if (roleLevelId == null)
            {
                ProjectRole projectRole = projectRoleService.getProjectRoleByName(roleLevel, errorCollection);
                if (projectRole != null && !errorCollection.hasAnyErrors())
                {
                    roleLevelId = projectRole.getId();
                }
                else
                {
                    throw new JellyTagException("ProjectRole level not found: " + roleLevel);
                }
            }
        }
        return (roleLevelId == null) ? null : roleLevelId.toString();
    }

    private void validateAttributes() throws JellyTagException
    {
        if (!paramSpecified(KEY_ISSUE_KEY) || !TextUtils.stringSet(getKey()))
        {
            throw new MissingAttributeException(KEY_ISSUE_KEY);
        }

        if (!paramSpecified(KEY_WORKFLOW_ACTION) || !TextUtils.stringSet(getWorkflowActionProperty()))
        {
            throw new MissingAttributeException(KEY_WORKFLOW_ACTION);
        }

        Issue issue = getIssue();

        ActionDescriptor actionDescriptor = getActionId(issue);
        WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
        FieldScreen fieldScreen = workflowActionsBean.getFieldScreenForView(actionDescriptor);

        // JRA-11768 - if the assignee has not been specified then specify it as the current assignee by default
        if (!getProperties().keySet().contains(IssueFieldConstants.ASSIGNEE))
        {
            // JRA--13412 - we only want to "auto-insert" an assignee IF there is a screen in play AND the screen contains assignee.
            // If we dont do this we will "fail" later in the field checks below.
            if (fieldScreen != null && fieldScreen.containsField(IssueFieldConstants.ASSIGNEE))
            {
                User assignee = issue.getAssignee();
                if (assignee != null)
                {
                    getProperties().put(IssueFieldConstants.ASSIGNEE, assignee.getName());
                }
            }
        }

        for (final Object o : getProperties().keySet())
        {
            String attributeName = (String) o;
            // Check if attribute is one that is allowed by this tag
            if (!attributes.contains(attributeName))
            {
                String fieldId = attributeName;
                int index = attributeName.indexOf(":");
                if (index > -1)
                {
                    fieldId = attributeName.substring(0, index);
                }

                // Check if the attribute is an orderable field
                if (!fieldManager.isOrderableField(fieldId))
                {
                    throw new JellyTagException("Invalid attribute '" + attributeName + "'.");
                }
                else if (fieldScreen == null)
                {
                    throw new JellyTagException("Field '" + attributeName + "' can not be set on action with no screen");
                }
                // Ensure the field actually appears on this screen
                else if (!fieldScreen.containsField(fieldId))
                {
                    throw new JellyTagException("Field '" + attributeName + "' is not present on screen '" + fieldScreen.getName() + "'.");
                }
            }
        }
    }

    private ActionDescriptor getActionByName(JiraWorkflow workflow, Issue issue) throws JellyTagException
    {
        boolean actionWithNameFound = false;
        String workflowActionProperty = getWorkflowActionProperty();
        // Try retrieving the action using its name
        for (ActionDescriptor actionDescriptor : workflow.getAllActions())
        {
            if (workflowActionProperty.equalsIgnoreCase(actionDescriptor.getName()))
            {
                // JRA-12374 - validate that this is a valid transition for the issue's current state, we don't want
                // to just match on the name otherwise two transitions with the same name may end up returning the
                // wrong action.
                if (issueWorkflowManager.isValidAction(issue, actionDescriptor.getId(), getApplicationUser()))
                {
                    return actionDescriptor;
                }
                else
                {
                    actionWithNameFound = true;
                }
            }
        }

        // Throw the right kind of error, if we found a transition with the right name but it was not valid for the
        // current state of the issue tell the user that.
        String errorMsg = actionWithNameFound ? getFoundButNotValidMessage(workflowActionProperty, issue) :
                "Invalid action name '" + workflowActionProperty + "'.";
        throw new JellyTagException(errorMsg);
    }

    /**
     * JRA-14164: must clear out the 'assignee' property before this tag is used again,
     * otherwise subsequent executions will use the stale value
     *
     * Note: if "assignee" was actually supplied in the tag, the value will be restored upon the next call of doTag
     * automatically
     */
    private void restoreProperties()
    {
        if (getProperties().containsKey(IssueFieldConstants.ASSIGNEE))
        {
            getProperties().remove(IssueFieldConstants.ASSIGNEE);
        }
    }

    private String getUsername()
    {
        return (String) getProperties().get(KEY_USER);
    }

    private String getKey()
    {
        return (String) getProperties().get(KEY_ISSUE_KEY);
    }

    private String getWorkflowActionProperty()
    {
        return (String) getProperties().get(KEY_WORKFLOW_ACTION);
    }

    private String getComment()
    {
        return (String) getProperties().get(KEY_COMMENT);
    }

    private String getGroupCommentLevel()
    {
        return (String) getProperties().get(KEY_GROUP_COMMENT_LEVEL);
    }

    private String getRoleCommentLevel()
    {
        return (String) getProperties().get(KEY_ROLE_COMMENT_LEVEL);
    }

    private boolean paramSpecified(String paramName)
    {
        return getProperties().containsKey(paramName);
    }

    public void addCustomFieldValue(CustomField customField, String customFieldValue, String key)
    {
        final String customFieldId = customField.getId();
        if (key != null)
        {
            getProperties().put(customFieldId + ":" + key, EasyList.build(customFieldValue));
        }
        else
        {
            if (paramSpecified(customFieldId))
            {
                // It's a multivalue, add to the list
                Collection values = (Collection) getProperties().get(customFieldId);
                values.add(customFieldValue);
            }
            else
            {
                getProperties().put(customFieldId, EasyList.build(customFieldValue));
            }
        }
    }
}