package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.util.ScreenTabErrorHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.security.random.SecureTokenGenerator;

import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CreateIssue extends AbstractIssueSelectAction implements Assignable, OperationContext
{
    protected final IssueCreationHelperBean issueCreationHelperBean;
    private final IssueFactory issueFactory;
    private final SecureTokenGenerator secureTokenGenerator = DefaultSecureTokenGenerator.getInstance();

    private Long pid;
    private String issuetype;
    protected Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    private FieldScreenRenderer fieldScreenRenderer;
    private SortedSet tabsWithErrors;
    private int selectedTab = 1;
    private Collection ignoreFieldIds = EasyList.build(IssueFieldConstants.ISSUE_TYPE);

    public CreateIssue(final IssueFactory issueFactory, final IssueCreationHelperBean issueCreationHelperBean)
    {
        this.issueFactory = issueFactory;
        this.issueCreationHelperBean = issueCreationHelperBean;
    }

    public String doDefault() throws Exception
    {
        issueCreationHelperBean.validateLicense(this, this);
        if (hasAnyErrors())
        {
            return "invalidlicense";
        }
        // set the project to the recently selected one (as a sensible default)
        // if they've been browsing issues in a project, makes sense they would want to add to same project
        Project current = getSelectedProjectObject();

        Long requestedPid = getPid();

        if (current != null && getAllowedProjects().contains(current.getGenericValue()))
        {
            if (pid == null)
            {
                pid = current.getId();
            }
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, pid);
        }

        String requestedIssueType = getIssuetype();
        setHistoryIssuetype();

        if (prepareFieldsIfOneOption(requestedPid, requestedIssueType))
        {
            return getRedirectForCreateBypass();
        }

        return super.doDefault();
    }

    protected String getRedirectForCreateBypass()
    {
        return forceRedirect("CreateIssue.jspa?pid=" + getPid() + "&issuetype=" + getIssuetype());
    }

    protected void setHistoryIssuetype()
    {
        if (issuetype == null)
        {
            issuetype = (String) ActionContext.getSession().get(SessionKeys.USER_HISTORY_ISSUETYPE);
        }

        if (issuetype == null)
        {
            // fall back to the default issue type
            issuetype = getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE);
        }

        getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, issuetype);
    }

    protected void doValidation()
    {
        try
        {
            issueCreationHelperBean.validateProject(getIssueObject(), this, ActionContext.getParameters(), this, this);
            if (!invalidInput())
            {
                getIssueObject().setProjectId(getPid());
            }

            issueCreationHelperBean.validateIssueType(getIssueObject(), this, ActionContext.getParameters(), this, this);
            if (!invalidInput())
            {
                getIssueObject().setIssueTypeId(getIssuetype());
            }
        }
        catch (Exception e)
        {
            log.error(e, e);
            addErrorMessage("An exception occurred: " + e + ".");
        }
    }

    protected void validateIssueType()
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
        issueTypeField.populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());
        issueTypeField.validateParams(this, this, this, getIssueObject(), getFieldScreenRenderer().getFieldScreenRenderLayoutItem(issueTypeField));
    }

    protected String doExecute() throws Exception
    {
        // validate their licence just in case they url hacked or came in via the create issue drop down
        // this overrides any other errors that may be in effect
        issueCreationHelperBean.validateLicense(this, this);
        if (hasAnyErrors())
        {
            return "invalidlicense";
        }

        // NOTE: this is passing null because the issueGV is null at this point and we can't
        // resolve a fieldLayoutItem to pass. For these two fields we are fine, since they are not renderable
        ProjectSystemField projectField = (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
        projectField.updateIssue(null, getIssueObject(), getFieldValuesHolder());

        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
        issueTypeField.updateIssue(null, getIssueObject(), getFieldValuesHolder());

        // Store last issue type, so it can be set as the default in the next issue the user files
        recordHistoryIssueType();

        // Store last project, so it can be set as the default in the next issue the user files
        setSelectedProjectId(getPid());

        //populate custom field values holder with default values
        populateFieldHolderWithDefaults(getIssueObject(), Collections.EMPTY_LIST);

        return SUCCESS;
    }

    public MutableIssue getIssueObject()
    {
        if (getIssueObjectWithoutDatabaseRead() == null)
        {
            setIssueObject(issueFactory.getIssue());
            // Most calls using the issue object will fail unless the issue object has the project and issue type are set
            getIssueObject().setProjectId(getPid());
            getIssueObject().setIssueTypeId(getIssuetype());
        }

        return getIssueObjectWithoutDatabaseRead();
    }

    protected void recordHistoryIssueType()
    {
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_ISSUETYPE, issuetype);
    }

    public Collection getAllowedProjects()
    {
        return getPermissionManager().getProjects(Permissions.CREATE_ISSUE, getLoggedInUser());
    }

    public boolean isAbleToCreateIssueInSelectedProject()
    {
        return getAllowedProjects().contains(getProject());
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public GenericValue getProject()
    {
        return getProjectManager().getProject(getPid());
    }

    public String getIssuetype()
    {
        return issuetype;
    }

    public void setIssuetype(String issueType)
    {
        this.issuetype = issueType;
    }

    public GenericValue getIssueTypeGV()
    {
        return ComponentAccessor.getConstantsManager().getIssueType(getIssuetype());
    }

    /**
     * This is just a 'niceness' method so that the default assignee selected on the next page, if assignees are
     * allowed, is the project lead.
     *
     * @deprecated
     */
    public String getAssignee()
    {
        return null;
    }

    /**
     * @deprecated
     */
    public void setAssignee(String assignee)
    {
        // Nothing to do
    }

    public List getFieldScreenRenderTabs()
    {
        return getFieldScreenRenderer().getFieldScreenRenderTabs();
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = issueCreationHelperBean.createFieldScreenRenderer(getIssueObject());
        }

        return fieldScreenRenderer;
    }


    public GenericValue getAssignIn() throws Exception
    {
        return getProject();
    }

    // Populate the fields of the specified issue with default values.
    // Specify a collection of field layout item ids to exclude these fields from being populated.
    protected void populateFieldHolderWithDefaults(Issue issue, Collection excludedFieldIds)
    {
        for (FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderTabLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                String fieldId = fieldScreenRenderTabLayoutItem.getOrderableField().getId();
                // If the field is not in the excluded list or not Project or Issue Type
                if (!excludedFieldIds.contains(fieldId) && !IssueFieldConstants.PROJECT.equals(fieldId) && !IssueFieldConstants.ISSUE_TYPE.equals(fieldId))
                {
                    fieldScreenRenderTabLayoutItem.populateDefaults(getFieldValuesHolder(), issue);
                }
            }
        }
    }

    public List<CustomField> getCustomFields(Issue issue)
    {
        return getCustomFieldManager().getCustomFieldObjects(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
    }

    public Map<String, Object> getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return IssueOperations.CREATE_ISSUE_OPERATION;
    }

    /**
     * Returns a list of {@link ButtonHolder}s for submit buttons required in addition to the standard 'submit' button.
     */
    public List getButtons() throws WorkflowException
    {
        List buttons = new ArrayList();
        Map buttonAttrs = getWorkflowMetaAttributes();
        final Iterator it = buttonAttrs.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String val = (String) buttonAttrs.get(key);
            buttons.add(new ButtonHolder(key, getText(val)));
        }
        return buttons;
    }

    /**
     * Returns <meta> attributes of the initial "create issue" workflow action.
     */
    private final Map getWorkflowMetaAttributes() throws WorkflowException
    {
        JiraWorkflow workflow = ManagerFactory.getWorkflowManager().getWorkflow(getPid(), getIssuetype());
        return workflow.getDescriptor().getInitialAction(1).getMetaAttributes();
    }

    /**
     * This is a special case where the has permissions should be informed that a new Issue is being created.
     */
    @Override
    public boolean isHasProjectPermission(int permissionsId, GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("isHasProjectPermission can not be passed a null project");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("isHasProjectPermission can only take a Project: " + project.getEntityName() + " is not.");
        }
        return getPermissionManager().hasPermission(permissionsId, project, getLoggedInUser(), true);
    }

    /**
     * This is a special case where the has permissions should be informed that a new Issue is being created.
     */
    @Override
    public boolean hasProjectPermission(int permissionsId, Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("isHasProjectPermission can not be passed a null project");
        }
        return getPermissionManager().hasPermission(permissionsId, project, getLoggedInApplicationUser(), true);
    }

    public Collection getTabsWithErrors()
    {
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return tabsWithErrors;
    }

    private void initTabsWithErrors()
    {
        tabsWithErrors = new TreeSet<FieldScreenRenderTab>();
        selectedTab = new ScreenTabErrorHelper().initialiseTabsWithErrors(tabsWithErrors, getErrors(), getFieldScreenRenderer(), ActionContext.getParameters());
    }

    public int getSelectedTab()
    {
        // Init tabs - as the first tab with error will be calculated then.
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return selectedTab;
    }

    /**
     * struct for holding name:value string pairs.
     */
    public static class ButtonHolder
    {

        private final String name;
        private final String value;

        public ButtonHolder(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }
    }

    public Collection getIgnoreFieldIds()
    {
        return ignoreFieldIds;
    }

    /**
     * Form submit button's i18n'ed name. Usually 'Create', but possibly overridden in workflow.
     */
    public String getSubmitButtonName()
    {
        JiraWorkflow workflow;
        try
        {
            workflow = ManagerFactory.getWorkflowManager().getWorkflow(getPid(), getIssuetype());
            return super.getWorkflowTransitionDisplayName(workflow.getDescriptor().getInitialAction(1));
        }
        catch (WorkflowException e)
        {
            log.error(e, e);
            return "Create";
        }
    }

    private boolean hasProjectIdRequestParamSet()
    {
        try
        {
            return new Long(request.getParameter("pid")) != null;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Checks if there is only one possible project and issue type combination given the possibly null requestedPid and
     * requestedIssueTypeId and the user's permission to create issues in the requested project.
     *
     * @param requestedPid the project id requested, null if none requested.
     * @param requestedIssueTypeId the issue type requested, null if none requested.
     * @return true only if there is one project/issuetype combo choosable for the user.
     */
    boolean prepareFieldsIfOneOption(Long requestedPid, String requestedIssueTypeId)
    {
        Long projectId = requestedPid;
        if (projectId == null)
        {
            final Collection projects = getAllowedProjects();
            if (projects.size() != 1)
            {
                return false;
            }
            else
            {
                projectId = ((GenericValue) projects.iterator().next()).getLong("id");
            }
        }

        final Project project = projectManager.getProjectObj(projectId);
        if (project != null)
        {
            Collection issueTypes = getIssueTypesForProject(project);
            if (requestedIssueTypeId == null)
            {
                if (issueTypes.size() == 1)
                {
                    IssueType issueType = (IssueType) issueTypes.iterator().next();
                    setPid(project.getId());
                    setIssuetype(issueType.getId());
                    return true;
                }
            }
            else
            {
                for (final Object issueType1 : issueTypes)
                {
                    IssueType issueType = (IssueType) issueType1;
                    if (issueType.getId().equals(requestedIssueTypeId))
                    {
                        // requested issue type
                        setPid(project.getId());
                        setIssuetype(requestedIssueTypeId);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns a collection of non-sub-task issue types for the given project.
     *
     * @param project project to get the issue types for
     * @return a collection of non-sub-task issue types for the given project
     */
    protected Collection getIssueTypesForProject(final Project project)
    {
        IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
        return issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }

}
