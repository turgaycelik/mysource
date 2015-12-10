package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.subtask.conversion.IssueConversionService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.ConvertIssueBean;
import com.atlassian.jira.web.bean.ConvertIssueToSubTaskBean;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract class for issue conversion
 * Contains all do* methods and vast majority of logic
 */
public abstract class AbstractConvertIssue extends JiraWebActionSupport
{
    protected static final String SECURITY_BREACH = "securitybreach";

    private final IssueConversionService service;
    protected final IssueManager issueManager;
    protected final ConstantsManager constantsManager;
    protected final FieldLayoutManager fieldLayoutManager;
    protected final WorkflowManager workflowManager;
    protected final PermissionManager permissionManager;
    protected final RendererManager rendererManager;
    private final IssueFactory issueFactory;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    protected static final int STEP_1 = 1;
    protected static final int STEP_2 = 2;
    protected static final int STEP_3 = 3;
    protected static final int STEP_4 = 4;

    protected Issue issue;
    private MutableIssue updatedIssue;

    protected String id;
    protected String issueType;
    protected String targetStatusId;
    protected String guid;
    private Collection layoutItems;
    private SearchRequest searchRequest;


    protected AbstractConvertIssue(IssueConversionService service,
            IssueManager issueManager, FieldLayoutManager fieldLayoutManager, ConstantsManager constantsManager,
            WorkflowManager workflowManager, RendererManager rendererManager, IssueFactory issueFactory,
            PermissionManager permissionManager)
    {
        this.service = service;
        this.issueManager = issueManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.rendererManager = rendererManager;
        this.issueFactory = issueFactory;
        this.permissionManager = permissionManager;
        this.sessionSearchObjectManagerFactory = ComponentAccessor.getComponent(SessionSearchObjectManagerFactory.class);
    }

    /**
     * First step of this wizard.
     * Gets the issue by given id. In case of error returns {@link #ERROR} view.
     * Checks user's permission, if denied returns {@link #SECURITY_BREACH} view.
     * Checks if given issue is convertable, if not returns {@link #ERROR} view.
     * Returns {@link #SUCCESS} view
     *
     * @return view
     */
    public String doStartWizard()
    {
        final JiraServiceContextImpl context = createServiceContext();

        initRequest(context);

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        if (!service.hasPermission(context, getIssue()))
        {
            return SECURITY_BREACH;
        }

        validateIssueIsConvertable(context);

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        setCurrentStep(STEP_1);

        return SUCCESS;
    }

    /**
     * Second step of this wizard.
     * Gets the issue by given id. In case of error returns {@link #ERROR} view.
     * Checks user's permission, if denied returns {@link #SECURITY_BREACH} view.
     * Checks if given issue is convertable and validates other values,
     * if not returns {@link #ERROR} view.
     * Returns view to update workflow or update fields view
     *
     * @return view
     */
    public String doSetIssueType()
    {
        final JiraServiceContextImpl context = createServiceContext();

        final ConvertIssueBean bean = getBean();
        setCurrentStep(STEP_1);

        initRequest(context);
        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        if (!service.hasPermission(context, getIssue()))
        {
            return SECURITY_BREACH;
        }

        validateIssueIsConvertable(context);

        validateStep1(context);

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        // Needs workflow change
        if (isStatusChangeRequired())
        {
            setCurrentStep(STEP_2);
            return SUCCESS + "_updateworkflow";
        }
        // Workflow change not needed, skip to field update
        else
        {
            // sets the target status to the staus of the current issue
            bean.setTargetStatusId(getIssue().getStatusObject().getId());

            populateDefaultFieldValues();

            if (context.getErrorCollection().hasAnyErrors())
            {
                return ERROR;
            }

            setCurrentStep(STEP_3);
            return SUCCESS + "_updatefields";  //skip workflow change. Goto field update
        }

    }


    /**
     * Third step of this wizard.
     * Gets the issue by given id. In case of error returns {@link #ERROR} view.
     * Checks user's permission, if denied returns {@link #SECURITY_BREACH} view.
     * Checks if given issue is convertable and valid values were entered,
     * if not returns {@link #ERROR} view.
     * Returns {@link #SUCCESS} view
     *
     * @return view
     */
    public String doSetWorkflowStatus()
    {
        final JiraServiceContextImpl context = createServiceContext();
        setCurrentStep(STEP_2);

        initRequest(context);
        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        if (!service.hasPermission(context, getIssue()))
        {
            return SECURITY_BREACH;
        }

        validateIssueIsConvertable(context);
        validateStep1(context);

        validateWorkflowStatus(context);

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        populateDefaultFieldValues();

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }
        setCurrentStep(STEP_3);
        return SUCCESS;
    }

    /**
     * Fourth step of this wizard.
     * Gets the issue by given id. In case of error returns {@link #ERROR} view.
     * Checks user's permission, if denied returns {@link #SECURITY_BREACH} view.
     * Checks if given issue is convertable and valid values were entered,
     * if not returns {@link #ERROR} view.
     * Returns {@link #SUCCESS} view
     *
     * @return view
     */
    public String doUpdateFields()
    {
        final JiraServiceContextImpl context = createServiceContext();
        getBean().setCurrentStep(STEP_3);

        initRequest(context);
        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        if (!service.hasPermission(context, getIssue()))
        {
            return SECURITY_BREACH;
        }

        validateIssueIsConvertable(context);
        validateStep1(context);
        validateWorkflowStatus(context);

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        // this also validates fields
        service.populateFields(context, getBean(), this, getTargetIssue(), getConvertFieldLayoutItems());

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }
        setCurrentStep(STEP_4);
        return SUCCESS;
    }

    /**
     * Last step of this wizard.
     * Gets the issue by given id. In case of error returns {@link #ERROR} view.
     * Checks user's permission, if denied returns {@link #SECURITY_BREACH} view.
     * Checks if given issue is convertable and valid values were entered,
     * if not returns {@link #ERROR} view.
     * <strong>Executes the conversion.</strong>
     * Returns to the issue
     *
     * @return view
     */
    @RequiresXsrfCheck
    public String doConvert()
    {
        final JiraServiceContextImpl context = createServiceContext();

        initRequest(context);
        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        if (!service.hasPermission(context, getIssue()))
        {
            return SECURITY_BREACH;
        }

        validateIssueIsConvertable(context);
        validateStep1(context);
        validateWorkflowStatus(context);

        service.validateFields(context, getBean(), this, getTargetIssue(), getConvertFieldLayoutItems());

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        service.convertIssue(context, getIssue(), getUpdatedIssue());

        if (context.getErrorCollection().hasAnyErrors())
        {
            return ERROR;
        }

        // get the key before clearing the session
        final String key = getIssue().getKey();

        getBean().clearSession(request.getSession());

        //check to make sure we've got the correct browse permission
        if (permissionManager.hasPermission(Permissions.BROWSE, getIssue(), context.getLoggedInUser()))
        {
            return getRedirect("/browse/" + key);
        }
        else
        {
            return getRedirect("CantBrowseCreatedIssue.jspa?issueKey=" + key + "&converted=true");
        }

    }

    /**
     * Handles the request to cancel the issue conversion wizard.
     * Clears the session and redirects to the view issue page for the issue in play.
     *
     * @return Redirects to the view issue page.
     */
    public String doCancel()
    {
        final Issue issue = getIssue();

        ConvertIssueBean bean = getBean();

        if (issue == null)
        {
            bean.clearSession(request.getSession());
            log.error("Could not retrieve issue for id:" + getId());
            return getRedirect("/");
        }
        else
        {
            // we need to get the issue key before it's cleared from session
            final String key = issue.getKey();
            bean.clearSession(request.getSession());
            return getRedirect("/browse/" + key);
        }
    }


    /**
     * Checks for valid state, populates the bean with values from parameters
     * and updates bean with new GUID
     *
     * @param context jira service context
     */
    protected void initRequest(JiraServiceContext context)
    {
        final ConvertIssueBean bean = getBean();

        if (!isValidIssueId(getId()) )
        {
            addI18nErrorMessage(context, "convert.issue.to.subtask.error.invalid.issuekey");
            return;
        }

        // validate issue
        Issue issue = getIssue();
        if (issue == null)
        {
            // Issue with given id not exists
            addI18nErrorMessage(context, "convert.issue.to.subtask.error.nosuchissue", getId());
            return;
        }

        // session timeout or browser back button after wizard finished
        // or somebody if putting GUID to url but there is no GUID in the bean in the session
        if (TextUtils.stringSet(guid) && bean.getVersion() == null)
        {
            addI18nErrorMessage(context, "convert.issue.to.subtask.errormessage.sessiontimeout");
            return;
        }

        // validate GUID

        if (TextUtils.stringSet(guid) && !guid.equals(bean.getVersion()))
        {
            addI18nErrorMessage(context, "convert.issue.to.subtask.errormessage.invalidstate", issue.getKey());
            return;
        }

        // Only set GUID if it hasn't been generated.
        if (!wasPassed("guid"))
        {
            //If setting this, assume session bean needs cleaning
            bean.clearBean();
            bean.generateNextVersion();

        }
        // populate bean with values from params
        if (wasPassed("issuetype"))
        {
            bean.setIssueType(issueType);
        }

        if (wasPassed("targetStatusId"))
        {
            bean.setTargetStatusId(targetStatusId);
        }

        initExtraFields(bean, context);
    }

    private boolean isValidIssueId(final String id)
    {
        try
        {
            Long.parseLong(id);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    // Validation methods

    /**
     * Validates that the given issue can be converted to the appropriated type
     *
     * @param context jira service context
     * @return True if the issue can be converted, false otherwise
     */
    private boolean validateIssueIsConvertable(JiraServiceContext context)
    {
        return service.canConvertIssue(context, getIssue());
    }

    /**
     * Validates the target issue type
     *
     * @param context jira service context
     */
    private void validateIssueType(JiraServiceContext context)
    {
        final String issueTypeId = getIssuetype();
        if (!TextUtils.stringSet(issueTypeId))
        {
            addI18nError(context, IssueFieldConstants.ISSUE_TYPE, "convert.issue.to.subtask.error.noissuetype");
            return;
        }

        IssueType issueTypeObject = constantsManager.getIssueTypeObject(issueTypeId);
        if (issueTypeObject == null)
        {
            addI18nError(context, IssueFieldConstants.ISSUE_TYPE, "convert.issue.to.subtask.error.nosuchissuetype");
            return;
        }

        service.validateTargetIssueType(context, getIssue(), issueTypeObject, IssueFieldConstants.ISSUE_TYPE);
    }

    /**
     * Validates that the issue can be converted to the given status
     *
     * @param context jira service context
     */
    private void validateWorkflowStatus(JiraServiceContext context)
    {
        final String fieldName = "targetStatusId";

        final String targetStatusId = getTargetStatusId();

        if (!TextUtils.stringSet(targetStatusId))
        {
            addI18nError(context, fieldName, "convert.issue.to.subtask.error.statusnotgiven", targetStatusId);
            return;
        }

        Status status = constantsManager.getStatusObject(targetStatusId);
        IssueType subTaskType = constantsManager.getIssueTypeObject(getBean().getIssueType());
        if (status == null)
        {
            addI18nError(context, fieldName, "convert.issue.to.subtask.error.statusdoesnotexist", targetStatusId);
            return;
        }

        service.validateTargetStatus(context, status, fieldName, getIssue(), subTaskType);
    }

    /**
     * Validates the first step of the wizard, subclasses can override to extend validation
     *
     * @param context jira service context
     */
    protected void validateStep1(JiraServiceContextImpl context)
    {
        validateIssueType(context);
    }

    /**
     * Retrieves the worked on issue based on the Issue id.
     *
     * @return Current Issue
     */
    public Issue getIssue()
    {
        if (issue == null)
        {
            Long id;
            try
            {
                id = Long.valueOf(getId());
            }
            catch (NumberFormatException e)
            {
                id = null;
            }

            if (id != null)
            {
                issue = issueManager.getIssueObject(id);
            }
        }
        return issue;
    }

    /**
     * Creates a {@link JiraServiceContext} to use in methods requiring a service call
     *
     * @return a {@link JiraServiceContext} based on current remote user and the
     */
    private JiraServiceContextImpl createServiceContext()
    {
        return new JiraServiceContextImpl(getLoggedInUser(), this);
    }


    /**
     * Returns true is the specified parameter was passed via request parameter value
     *
     * @param param parameter to check
     * @return true if value passed via request, false otherwise
     */
    protected boolean wasPassed(String param)
    {
        return ActionContext.getParameters().containsKey(param);
    }

    /**
     * Retrieves the wizard bean from the session. Never returns null, if no
     * bean is found new one is created and stored in the session.
     *
     * @return wizard bean
     */
    protected ConvertIssueBean getBean()
    {
        return ConvertIssueBean.getBean(request.getSession(), getId());
    }


    /**
     * Populates the CustomFieldValuesHolder with the default values
     */
    private void populateDefaultFieldValues()
    {
        Issue targetIssue = getTargetIssue();
        Issue origIssue = getIssue();
        Collection fieldItems = service.getFieldLayoutItems(origIssue, targetIssue);

        for (final Object fieldItem : fieldItems)
        {
            FieldLayoutItem item = (FieldLayoutItem) fieldItem;
            OrderableField orderableField = item.getOrderableField();

            if (orderableField.isShown(targetIssue))
            {
                orderableField.populateForMove(getBean().getFieldValuesHolder(), origIssue, targetIssue);
            }
        }
    }

    /**
     * Get Target Issue with Security Level set to null if it is needed.
     * JRA-11605 - we need this method so that the display can prompt the user with values from system fields that
     * are unencumbered by security level permission checks.
     *
     * @return Target Issue with nullified Security Level
     */
    public MutableIssue getTargetIssue()
    {
        MutableIssue targetIssue = getTargetIssueObjectWithSecurityLevel();

        FieldLayout layout = fieldLayoutManager.getFieldLayout(getIssue());
        FieldLayoutItem fieldLayoutItem = layout.getFieldLayoutItem(IssueFieldConstants.SECURITY);
        if (fieldLayoutItem != null)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();
            if (orderableField.needsMove(EasyList.build(getIssue()), targetIssue, fieldLayoutItem).getResult())
            {
                targetIssue.setSecurityLevel(null);
            }

        }
        return targetIssue;
    }

    /**
     * Get the issue with updated Issue type and status.
     * Subclasses can over-ride if they need to set their own fields.
     * E.g Issue to sub-task conversion sets the new parent issue.
     * Security level remains intact.
     *
     * @return Cloned issue with updated issue ype and status
     */
    protected MutableIssue getTargetIssueObjectWithSecurityLevel()
    {
        final ConvertIssueBean bean = getBean();

        MutableIssue targetIssue = issueManager.getIssueObject(getIssue().getId());
        targetIssue = issueFactory.cloneIssueNoParent(targetIssue);

        targetIssue.setIssueTypeId(bean.getIssueType());
        if (bean.getTargetStatusId() != null)
        {
            targetIssue.setStatusId(bean.getTargetStatusId());
        }

        return targetIssue;
    }

    /**
     * Whether or not the issue's status is valid in the workflow
     *
     * @return true if status needs updating, false otherwise
     */
    public boolean isStatusChangeRequired()
    {
        Issue issue = getIssue();
        JiraServiceContext context = createServiceContext();

        // assume Issue Type has been validated
        IssueType subTaskType = constantsManager.getIssueTypeObject(getBean().getIssueType());

        return service.isStatusChangeRequired(context, issue, subTaskType);
    }

    /**
     * Gets the target issue with new values set on issue
     *
     * @return Issue with all fields updated and hidden field removed
     */
    public MutableIssue getUpdatedIssue()
    {
        if (updatedIssue == null)
        {
            // JRA-11605 - we need the real target issue object including the security level so that the changed value
            // of the security level will be correctly recorded by the field. This also has the effect of letting the
            // system field update the subtask issue security level (handled by the field but probably should not be).
            MutableIssue targetIssue = getTargetIssueObjectWithSecurityLevel();

            for (final Object o : getConvertFieldLayoutItems())
            {
                FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o;
                OrderableField orderableField = fieldLayoutItem.getOrderableField();
                orderableField.updateIssue(fieldLayoutItem, targetIssue, getBean().getFieldValuesHolder());
            }

            Collection removedFields = getRemoveFields();
            for (final Object removedField : removedFields)
            {
                OrderableField field = (OrderableField) removedField;
                field.removeValueFromIssueObject(targetIssue);
            }

            updatedIssue = targetIssue;
        }

        return updatedIssue;
    }

    /**
     * Returns a list of {@link FieldLayoutItem} that need values for conversion.
     *
     * @return list of {@link FieldLayoutItem}
     */
    public Collection<FieldLayoutItem> getConvertFieldLayoutItems()
    {
        if (layoutItems == null)
        {
            layoutItems = service.getFieldLayoutItems(getIssue(), getTargetIssue());
        }
        return layoutItems;
    }

    // View Utility methods

    /**
     * Returns a list of {@link com.atlassian.jira.issue.fields.OrderableField}s that should be removed from the target issue.
     *
     * @return a list of {@link com.atlassian.jira.issue.fields.OrderableField}s that should be removed from the target issue.
     */
    public Collection /* <OrderableField> */ getRemoveFields()
    {
        return service.getRemovedFields(getIssue(), getTargetIssue());
    }

    /**
     * Utility method used by convertissuetosubtask-confirm.jsp and
     * convertissuetosubtask-confirm-part1.jsp
     * <br/>
     * Retrieve and return workflow for selected issue's project and new
     * (target) issue type.
     *
     * @return target workflow
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if WorkflowException occured during retrieval
     */
    public JiraWorkflow getTargetWorkflow()
    {
        final Long projectId = getIssue().getProjectObject().getId();
        final String issueTypeId = getBean().getIssueType();
        try
        {
            return workflowManager.getWorkflow(projectId, issueTypeId);
        }
        catch (WorkflowException e)
        {
            String msg = "Failed retrieving workflow for project: " + projectId + " and issue type:" + issueTypeId;
            log.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Utility method used by convertissuetosubtask-confirm.jsp
     *
     * @param field filed
     * @return field name
     */
    public String getFieldName(Field field)
    {
        if (field instanceof CustomField)
        {
            return field.getName();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    /**
     * Gets the old view of the field before update
     *
     * @param field Field to display
     * @return HTML to insert
     */
    public String getOldViewHtml(OrderableField field)
    {
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(getIssue());
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);


        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("prefix", "old_").toMutableMap();

        return field.getViewHtml(fieldLayoutItem, this, getIssue(), displayParams);
    }

    /**
     * Gets the ne view of the field after values have been updated
     *
     * @param field Field to display
     * @return HTML to insert
     */
    public String getNewViewHtml(OrderableField field)
    {
        MutableIssue updatedIssue = getUpdatedIssue();
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(updatedIssue);
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);
        String html;

        // NOTE: This is a HACK!!!!
        // Renderable custom fields always retrieve values from the database, bot from issue passed in.
        if (field instanceof CustomField && ((CustomField) field).isRenderable())
        {
            final String rendererType = fieldLayoutItem.getRendererType();
            final String unrenderedValue = (String) updatedIssue.getCustomFieldValue((CustomField) field);
            final IssueRenderContext renderContext = updatedIssue.getIssueRenderContext();

            html = rendererManager.getRenderedContent(rendererType, unrenderedValue, renderContext);
            if (html == null || "".equals(html.trim()))
            {
                html = "&nbsp;";
            }
        }
        else
        {
            final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("prefix", "new_").toMutableMap();

            html = field.getViewHtml(fieldLayoutItem, this, updatedIssue, displayParams);
        }
        return html;
    }

    /**
     * Gets the create HTML for the given FieldLayoutItem.
     *
     * @param fieldLayoutItem Layoutitem describing field to display
     * @return HTML to insert
     */
    public String getFieldHtml(FieldLayoutItem fieldLayoutItem)
    {

        OrderableField orderableField = fieldLayoutItem.getOrderableField();
        MutableIssue targetIssue = getTargetIssue();
        if (orderableField.isShown(targetIssue))
        {
            return orderableField.getCreateHtml(fieldLayoutItem, getBean(), this, targetIssue);
        }
        else
        {
            return "";
        }
    }

    /**
     * Utility method used by convertissuetosubtask-updateworkflow.jsp and
     * convertissuetosubtask-confirm-part1.jsp
     * <br/>
     * Retrieve and return current workflow for selected issue
     *
     * @return current workflow
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if WorkflowException occured during retrieval
     */
    public JiraWorkflow getCurrentWorkflow()
    {
        Issue issue = getIssue();
        try
        {
            return workflowManager.getWorkflow(issue);
        }
        catch (WorkflowException e)
        {
            String msg = "Failed retrieving workflow for issue: " + issue.getKey();
            log.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * This method will return the one in the current search request, or return null if one does not exist.
     * @return The current search request, or null if there is no current search request.
     */
    public SearchRequest getSearchRequest()
    {
        if (searchRequest == null)
        {
            searchRequest = getSearchRequestFromSession();
        }
        return searchRequest;
    }

    private SearchRequest getSearchRequestFromSession()
    {
        final SessionSearchRequestManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createSearchRequestManager();
        return sessionSearchRequestManager.getCurrentObject();
    }

    /**
     * Returns the JQL representation of the current search request in the session.
     *
     * @return the jql of the SearchRequest in the session, empty string otherwise.
     */
    public String getCurrentJQL()
    {
        final SearchRequest sr = getSearchRequest();
        if (sr != null)
        {
            final SearchService searchService = ComponentManager.getInstance().getSearchService();
            return searchService.getJqlString(sr.getQuery());
        }
        return null;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getParentIssueKey()
    {
        return ((ConvertIssueToSubTaskBean) getBean()).getParentIssueKey();
    }

    public String getTargetStatusId()
    {
        return getBean().getTargetStatusId();
    }

    public void setTargetStatusId(String targetStatusId)
    {
        this.targetStatusId = targetStatusId;
    }

    public String getIssuetype()
    {
        return getBean().getIssueType();
    }

    public void setIssuetype(String subTaskType)
    {
        this.issueType = subTaskType;
    }

    public String getGuid()
    {
        return getBean().getVersion();
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
    }

    public int getCurrentStep()
    {
        return getBean().getCurrentStep();
    }

    protected void setCurrentStep(int step)
    {
        getBean().setCurrentStep(step);
    }

    // Utility methods

    /**
     * Conveniece method for adding internationalized error messages to the error collection
     *
     * @param context jira service context
     * @param i18nKey message key
     */
    protected void addI18nErrorMessage(JiraServiceContext context, String i18nKey)
    {
        context.getErrorCollection().addErrorMessage(getText(i18nKey));
    }

    /**
     * Convenience method for adding internationalized error messages to the error collection
     *
     * @param context jira service context
     * @param i18nKey message key
     * @param param   parameter value
     */
    protected void addI18nErrorMessage(JiraServiceContext context, String i18nKey, String param)
    {
        context.getErrorCollection().addErrorMessage(getText(i18nKey, param));
    }

    /**
     * Convenience method for adding internationalized errors to the error collection
     *
     * @param context   jira service context
     * @param fieldName field name
     * @param i18nKey   message key
     */
    protected void addI18nError(JiraServiceContext context, String fieldName, String i18nKey)
    {
        context.getErrorCollection().addError(fieldName, getText(i18nKey));
    }

    /**
     * Convenience method for adding internationalized errors to the error collection
     *
     * @param context   jira service context
     * @param fieldName field name
     * @param i18nKey   message key
     * @param param     parameter value
     */
    protected void addI18nError(JiraServiceContext context, String fieldName, String i18nKey, String param)
    {
        context.getErrorCollection().addError(fieldName, getText(i18nKey, param));
    }

    /**
     * Utility method to get appropriate key for property.
     *
     * @param key to get
     * @return key prefixed with action specific prefix.
     */
    public String getTextKey(String key)
    {
        return getPropertiesPrefix() + "." + key;
    }

    public String getIssuePath()
    {
        MutableIssue targetIssue = issueManager.getIssueObject(getIssue().getId());
        targetIssue = issueFactory.cloneIssueNoParent(targetIssue);

        return "/browse/" + targetIssue.getKey();
    }

    /**
     * Prefix used for jsps Actions.  Allows the same jsp to be used for different actions
     *
     * @return subclass specific action prefix
     */
    public abstract String getActionPrefix();

    /**
     * Prefix used for i18n properties.  Allows the same jsp to be used for different actions
     *
     * @return subclass specific properties prefix
     */
    public abstract String getPropertiesPrefix();

    /**
     * Gets a collection of {@link IssueType} that are the available target Issue Types.
     *
     * @return a collection of {@link IssueType}
     */
    public abstract Collection /* <IssueType> */ getAvailableIssueTypes();

    /**
     * Allows subclasses to set and init own fields.
     * E.g. ConvertIssueToSubTask sets parent id.
     *
     * @param bean    bean that holds params.
     * @param context jira service context.
     */
    protected abstract void initExtraFields(ConvertIssueBean bean, JiraServiceContext context);
}
