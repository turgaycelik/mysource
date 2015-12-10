package com.atlassian.jira.bc.issue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.events.WorkflowManualTransitionExecutionEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowProgressAware;

import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * The default implementation of the IssueService.
 *
 * @since v4.1
 */
public class DefaultIssueService implements IssueService
{
    private static final Logger log = Logger.getLogger(DefaultIssueService.class);

    private final IssueFactory issueFactory;
    private final IssueCreationHelperBean issueCreationHelperBean;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final WorkflowManager workflowManager;
    private final IssueWorkflowManager issueWorkflowManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final EventPublisher eventPublisher;

    public DefaultIssueService(IssueFactory issueFactory, IssueCreationHelperBean issueCreationHelperBean,
            FieldManager fieldManager, IssueManager issueManager, PermissionManager permissionManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, WorkflowManager workflowManager,
            IssueWorkflowManager issueWorkflowManager, FieldLayoutManager fieldLayoutManager, FieldConfigSchemeManager fieldConfigSchemeManager,
            EventPublisher eventPublisher)
    {
        this.issueFactory = issueFactory;
        this.issueCreationHelperBean = issueCreationHelperBean;
        this.fieldManager = fieldManager;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.workflowManager = workflowManager;
        this.issueWorkflowManager = issueWorkflowManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.eventPublisher = eventPublisher;
    }

    public IssueResult getIssue(User user, Long issueId)
    {
        final I18nHelper i18n = getI18n(user);

        final MutableIssue issue = issueManager.getIssueObject(issueId);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        return new IssueResult(getIssue(user, issue, i18n, errors), errors);
    }

    @Override
    public IssueResult getIssue(User user, String issueKey)
    {
        final I18nHelper i18n = getI18n(user);

        final MutableIssue issue = issueManager.getIssueObject(issueKey);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        return new IssueResult(getIssue(user, issue, i18n, errors), errors);
    }

    @Override
    public CreateValidationResult validateCreate(User user, IssueInputParameters issueInputParameters)
    {
        if (issueInputParameters == null)
        {
            throw new IllegalArgumentException("Can not validate issue creation with a null IssueInputParameters.");
        }
        // Create a MutableIssue to work with
        MutableIssue issue = constructNewIssue();

        return validateCreate(user, issue, issueInputParameters);
    }

    @Override
    public CreateValidationResult validateSubTaskCreate(User user, Long parentId, IssueInputParameters issueInputParameters)
    {
        // Create a MutableIssue to work with
        MutableIssue issue = constructNewIssue();
        // Because we are creating a subTask we need to make sure we set the parentId on the issue
        issue.setParentId(parentId);

        return validateCreate(user, issue, issueInputParameters);
    }

    @Override
    public IssueResult create(User user, CreateValidationResult createValidationResult)
    {
        return create(user, createValidationResult, null);
    }

    @Override
    public IssueResult create(User user, CreateValidationResult createValidationResult, String initialWorkflowActionName)
    {
        if (createValidationResult == null)
        {
            throw new IllegalArgumentException("You can not create an issue with a null validation result.");
        }

        if (!createValidationResult.isValid())
        {
            throw new IllegalStateException("You can not create an issue with an invalid validation result.");
        }

        if (createValidationResult.getIssue() == null)
        {
            throw new IllegalArgumentException("You can not create a null issue.");
        }

        final Map<String, Object> fields = new HashMap<String, Object>();
        final MutableIssue issue = createValidationResult.getIssue();
        fields.put("issue", issue);
        final MutableIssue originalIssue = issueManager.getIssueObject(issue.getId());
        fields.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
        fields.put("pkey", issue.getProjectObject().getKey());
        if (initialWorkflowActionName != null)
        {
            fields.put("submitbutton", initialWorkflowActionName); // let the workflow know which submit button was pressed
        }

        final GenericValue newIssueGV;
        try
        {
            newIssueGV = issueManager.createIssue(user, fields);
            return new IssueResult(issueFactory.getIssue(newIssueGV));
        }
        catch (CreateException e)
        {
            final ErrorCollection errors = new SimpleErrorCollection();
            handleCreateException(getI18n(user), errors, e);
            return new IssueResult(null, errors);
        }
    }

    @Override
    public UpdateValidationResult validateUpdate(User user, Long issueId, IssueInputParameters issueInputParameters)
    {
        if (issueInputParameters == null)
        {
            throw new IllegalArgumentException("You must provide a non-null issueInputParameters to update an issue.");
        }

        final I18nHelper i18n = getI18n(user);

        final ErrorCollection errors = new SimpleErrorCollection();
        final Map<String, Object> fieldValuesHolder = cloneFieldValuesHolder(issueInputParameters);

        final HistoryMetadata historyMetadata = issueInputParameters.getHistoryMetadata();
        if (issueId == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.update.issue.is.null"));
            return new UpdateValidationResult(null, errors, fieldValuesHolder, historyMetadata);
        }

        // Try to lookup the issue that we must update
        final MutableIssue issue = issueManager.getIssueObject(issueId);

        if (issue == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.update.issue.is.null"));
            return new UpdateValidationResult(null, errors, fieldValuesHolder, historyMetadata);
        }

        if (!hasPermissionToEdit(user, issue, i18n, errors))
        {
            return new UpdateValidationResult(null, errors, fieldValuesHolder, historyMetadata);
        }

        final ErrorCollection errorCollection = new SimpleErrorCollection();

        // We want to copy the issue so that when the fields are doing their things we are not modifying the passed
        // in issue reference.
        MutableIssue copiedIssue = copyIssue(issue);

        MutableIssue updatedIssue = validateAndUpdateIssueFromFields(user, copiedIssue, issueInputParameters, fieldValuesHolder, errorCollection,
                i18n, getUpdateFieldScreenRenderer(user, issue), true, null);
        if (errorCollection.hasAnyErrors())
        {
            updatedIssue = null;
            errors.addErrorCollection(errorCollection);
        }

        return new UpdateValidationResult(updatedIssue, errors, fieldValuesHolder, historyMetadata);
    }

    @Override
    public IssueResult update(User user, UpdateValidationResult issueValidationResult)
    {
        return update(user, issueValidationResult, EventDispatchOption.ISSUE_UPDATED, true);
    }

    @Override
    public IssueResult update(User user, UpdateValidationResult issueValidationResult, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        if (issueValidationResult == null)
        {
            throw new IllegalArgumentException("You can not update an issue with a null validation result.");
        }
        if (!issueValidationResult.isValid())
        {
            throw new IllegalStateException("You can not update an issue with an invalid validation result.");
        }
        final MutableIssue issue = issueValidationResult.getIssue();
        if (eventDispatchOption == null)
        {
            throw new IllegalArgumentException("You can not update an issue with a null EventDispatchOption.");
        }
        if (issue == null)
        {
            throw new IllegalArgumentException("You can not update a null issue.");
        }
        final MutableIssue updatedIssue;
        try
        {
            updatedIssue = (MutableIssue) issueManager.updateIssue(
                        ApplicationUsers.from(user),
                        issue, UpdateIssueRequest.builder()
                            .eventDispatchOption(eventDispatchOption)
                            .sendMail(sendMail)
                            .historyMetadata(issueValidationResult.getHistoryMetadata())
                            .build());
        }
        catch (RuntimeException e)
        {
            log.error("Exception occurred editing issue: " + e, e);
            final ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(getI18n(user).getText("admin.errors.issues.exception.occured", e));
            return new IssueResult(null, errors);
        }
        return new IssueResult(updatedIssue);
    }

    @Override
    public DeleteValidationResult validateDelete(User user, Long issueId)
    {
        final I18nHelper i18n = getI18n(user);

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        if (issueId == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.delete.issue.is.null"));
            return new DeleteValidationResult(null, errors);
        }

        // Try to lookup the issue that we must update
        final MutableIssue issue = issueManager.getIssueObject(issueId);

        if (issue == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.delete.issue.is.null"));
            return new DeleteValidationResult(null, errors);
        }

        if (hasPermissionToDelete(user, issue, i18n, errors))
        {
            return new DeleteValidationResult(issue, errors);
        }
        else
        {
            return new DeleteValidationResult(null, errors);
        }
    }

    @Override
    public ErrorCollection delete(User user, DeleteValidationResult issueValidationResult)
    {
        return delete(user, issueValidationResult, EventDispatchOption.ISSUE_DELETED, true);
    }

    @Override
    public ErrorCollection delete(User user, DeleteValidationResult issueValidationResult, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        if (eventDispatchOption == null)
        {
            throw new IllegalArgumentException("You can not delete an issue with a null EventDispatchOption.");
        }

        if (issueValidationResult == null)
        {
            throw new IllegalArgumentException("You can not delete an issue with a null IssueValidationResult.");
        }

        if (!issueValidationResult.isValid())
        {
            throw new IllegalStateException("You can not delete an issue with an invalid validation result.");
        }

        if (issueValidationResult.getIssue() == null)
        {
            throw new IllegalArgumentException("You can not delete a null issue.");
        }

        final MutableIssue issue = issueValidationResult.getIssue();
        final ErrorCollection errors = new SimpleErrorCollection();
        try
        {
            issueManager.deleteIssue(user, issue, eventDispatchOption, sendMail);
        }
        catch (RemoveException e)
        {
            log.error("There was an exception while trying to delete the issue '" + issue.getKey() + "'.", e);
            errors.addErrorMessage(getI18n(user).getText("issue.service.issue.deletion.error", issue.getKey()));
        }
        return errors;
    }

    @Override
    public boolean isEditable(final Issue issue, final User user)
    {
        return issueManager.isEditable(issue, user);
    }

    @Override
    public TransitionValidationResult validateTransition(User user, Long issueId, int actionId, IssueInputParameters issueInputParameters)
    {
        return validateTransition(user, issueId, actionId, issueInputParameters, TransitionOptions.defaults());
    }

    @Override
    public TransitionValidationResult validateTransition(User user, Long issueId, int actionId, IssueInputParameters issueInputParameters, TransitionOptions transitionOptions)
    {
        if (issueInputParameters == null)
        {
            throw new IllegalArgumentException("You must provide a non-null issueInputParameters.");
        }
        final I18nHelper i18n = getI18n(user);
        final Map<String, Object> fieldValuesHolder = cloneFieldValuesHolder(issueInputParameters);
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        if (issueId == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.transition.issue.is.null"));
            return new TransitionValidationResult(null, errors, fieldValuesHolder, null, actionId);
        }

        // Try to lookup the issue that we must update
        final MutableIssue issue = issueManager.getIssueObject(issueId);

        if (issue == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.transition.issue.is.null"));
            return new TransitionValidationResult(null, errors, fieldValuesHolder, null, actionId);
        }
        String originalAssigneeId = issue.getAssigneeId();

        final ActionDescriptor actionDescriptor = getActionDescriptor(issue, actionId);

        // Validate that the action exists
        if (actionDescriptor == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.transition.issue.no.action", String.valueOf(actionId)));
            return new TransitionValidationResult(null, errors, fieldValuesHolder, null, actionId);
        }

        // Validate that the action is a valid action
        if (!issueWorkflowManager.isValidAction(issue, actionId, transitionOptions, ApplicationUsers.from(user)))
        {
            errors.addErrorMessage(i18n.getText("issue.service.transition.issue.action.invalid", actionDescriptor.getName(), issue.getKey()));
            return new TransitionValidationResult(null, errors, fieldValuesHolder, null, actionId);
        }

        MutableIssue updatedIssue;
        // We only want to update the issue if there is an associated screen with the transition
        if (StringUtils.isNotEmpty(actionDescriptor.getView()))
        {
            updatedIssue = validateAndUpdateIssueFromFields(user, issue, issueInputParameters, fieldValuesHolder,
                    errors, i18n, getTransitionFieldScreenRenderer(user, issue, actionDescriptor), false, actionId);

            if (errors.hasAnyErrors())
            {
                return new TransitionValidationResult(null, errors, fieldValuesHolder, null, actionId);
            }
            // Comment information is handled by the generated change history so no need to put it into the additional
            // parameters
        }
        else
        {
            updatedIssue = issue;
        }

        final Map<String, Object> additionalParams = createAdditionalParameters(ApplicationUsers.from(user), fieldValuesHolder, transitionOptions, issueInputParameters.getHistoryMetadata(), originalAssigneeId);
        return new TransitionValidationResult(updatedIssue, errors, fieldValuesHolder, additionalParams, actionId);
    }

    @Override
    public IssueResult transition(User user, TransitionValidationResult transitionResult)
    {
        if (transitionResult == null)
        {
            throw new IllegalArgumentException("You must provide a non-null transition result to transition an issue through workflow.");
        }
        if (!transitionResult.isValid())
        {
            throw new IllegalStateException("You can not transition an issue with an invalid validation result.");
        }

        final MutableIssue issue = transitionResult.getIssue();

        if (issue == null)
        {
            throw new IllegalArgumentException("You can not transition a null issue.");
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ApplicationUser appUser = ApplicationUsers.from(user);
        WorkflowProgressAware workflowProgressAware = new SimpleWorkflowProgressAware(transitionResult.getActionId(), appUser, errorCollection, transitionResult.getAdditionInputs(), issue);
        workflowManager.doWorkflowAction(workflowProgressAware);

        IssueResult issueResult= new IssueResult(issueManager.getIssueObject(issue.getId()), errorCollection);
        triggerEventsForManualTransition(issueResult, transitionResult);
        return issueResult;
    }

    private void triggerEventsForManualTransition(IssueResult issueResult, TransitionValidationResult transitionResult)
    {
        TransitionOptions transitionOptions= TransitionOptions.toTransitionOptions(transitionResult.getAdditionInputs());

        if (!transitionOptions.isAutomaticTransition())
        {
            eventPublisher.publish(new WorkflowManualTransitionExecutionEvent(issueResult.getIssue().getKey(), transitionResult.getActionId(), issueResult.isValid()));
            Instrumentation.pullCounter(InstrumentationName.WORKFLOW_MANUAL_TRANSITION).incrementAndGet();
        }
        else
        {
            Instrumentation.pullCounter(InstrumentationName.WORKFLOW_AUTOMATIC_TRANSITION).incrementAndGet();
        }
    }

    @Override
    public AssignValidationResult validateAssign(User user, Long issueId, String assignee)
    {
        final I18nHelper i18n = getI18n(user);
        final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
        OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
        fieldValuesHolder.put(field.getId(), assignee);

        OperationContext operationContext = new OperationContext()
        {
            @Override
            public Map<String, Object> getFieldValuesHolder()
            {
                return fieldValuesHolder;
            }

            @Override
            public IssueOperation getIssueOperation()
            {
                return IssueOperations.EDIT_ISSUE_OPERATION;
            }
        };
        SimpleErrorCollection errors = new SimpleErrorCollection();
        if (issueId == null)
        {
            errors.addErrorMessage(i18n.getText("issue.service.transition.issue.is.null"));
            return new AssignValidationResult(null, errors, assignee);
        }

        // Try to lookup the issue that we must update
        final MutableIssue issue = issueManager.getIssueObject(issueId);
        // Validate the request
        field.validateParams(operationContext, errors, i18n, issue, null);

        return new AssignValidationResult(issue, errors, assignee);
    }

    @Override
    public IssueResult assign(final User user, AssignValidationResult assignResult)
    {
        final MutableIssue issue = assignResult.getIssue();
        // update the assignee system field
        OrderableField field = (OrderableField) fieldManager.getField(IssueFieldConstants.ASSIGNEE);
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field);
        field.updateIssue(fieldLayoutItem, issue, EasyMap.build(field.getId(), assignResult.getAssigneeId()));

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_ASSIGNED, true);
        }
        catch (RuntimeException e)
        {
            log.error("There was an exception while trying to delete the issue '" + issue.getKey() + "'.", e);
            errorCollection.addErrorMessage(getI18n(user).getText("issue.service.issue.deletion.error", issue.getKey()));
        }
        return new IssueResult(issue, errorCollection);
    }

    @Override
    public IssueInputParameters newIssueInputParameters()
    {
        return new IssueInputParametersImpl();
    }

    @Override
    public IssueInputParameters newIssueInputParameters(Map<String, String[]> actionParameters)
    {
        return new IssueInputParametersImpl(actionParameters);
    }

    private void handleCreateException(final I18nHelper i18n, final ErrorCollection errors, final CreateException createException)
    {
        final String errMsg = i18n.getText("admin.errors.issues.error.creating", createException.getMessage());

        Throwable cause = createException.getCause();
        if (cause instanceof InvalidInputException)
        {
            InvalidInputException inputException = (InvalidInputException) cause;
            handleInvalidInputException(errors, inputException);
        } else
        {
            errors.addErrorMessage(errMsg);
        }
    }

    /**
     * If the exception was an InvalidInputException then we have some information about what fields are in error.
     *
     * Its up to the implementers of the Validator to get the fields names right, and i18nized and so on for the current
     * screen scheme.
     *
     * @param inputException the workflow input exception
     */
    private void handleInvalidInputException(final ErrorCollection errors, final InvalidInputException inputException)
    {
        for (final Object o1 : inputException.getGenericErrors())
        {
            String error = (String) o1;
            errors.addErrorMessage(error);
        }

        for (final Object o : inputException.getErrors().entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            errors.addError((String) entry.getKey(), (String) entry.getValue());
        }
    }

    @Nullable
    MutableIssue validateAndUpdateIssueFromFields(User user, MutableIssue issue, IssueInputParameters issueInputParameters,
            Map<String, Object> fieldValuesHolder, ErrorCollection errorCollection, I18nHelper i18n,
            final FieldScreenRenderer fieldScreenRenderer, boolean updateComment, @Nullable Integer workflowActionId)
    {
        final OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, fieldValuesHolder);
        final ErrorCollection localCollection = new SimpleErrorCollection();

        // Check that the incoming values are valid values
        if (workflowActionId == null)
        {
            validateAndPopulateParams(user, issue, issueInputParameters, fieldValuesHolder, operationContext, localCollection, i18n, fieldScreenRenderer);
        }
        else
        {
            validateAndPopulateParamsForWorkflowTransition(user, issue, issueInputParameters, fieldValuesHolder, operationContext, localCollection, i18n, fieldScreenRenderer, workflowActionId);
        }

        // Update the issue with the passed in values if we have passed all the validation
        if (!localCollection.hasAnyErrors())
        {
            updateIssueFromFields(fieldScreenRenderer, issue, user, fieldValuesHolder, updateComment, issueInputParameters);
        }
        else
        {
            issue = null;
            errorCollection.addErrorCollection(localCollection);
        }

        return issue;
    }

    CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
    {
        final I18nHelper i18n = getI18n(user);

        if (issueInputParameters == null)
        {
            throw new IllegalArgumentException("You must provide non-null issue input parameters to validate an issue.");
        }
        if (issue == null)
        {
            throw new IllegalArgumentException("You must provide a non-null issue to validate and populate.");
        }

        final ErrorCollection errorCollection = new SimpleErrorCollection();

        // Create a copy of the FieldValuesHolder provided with the IssueInputParameters
        final Map<String, Object> fieldValuesHolder = cloneFieldValuesHolder(issueInputParameters);

        // First check that the the license still allows the users to create the issue
        if (!licenseInvalidForIssueCreation(errorCollection, i18n))
        {
            // Validate that the project is valid and if so then set it on the new issue
            issue = validateAndSetProject(issue, issueInputParameters, fieldValuesHolder, errorCollection, i18n);

            // Validate that the issueType is valid and if so then set it on the new issue
            issue = validateAndSetIssueType(issue, issueInputParameters, fieldValuesHolder, errorCollection, i18n);

            if (!errorCollection.hasAnyErrors())
            {

                // Validate the user has the create issue permission even though this is already done by the ProjectSystemField
                if (hasPermissionToCreate(user, issue.getProjectObject(), i18n, errorCollection))
                {
                    // Validate all the input fields and if valid set them on the new issue, if invalid we return null
                    issue = validateAndCreateIssueFromFields(user, issue, issueInputParameters, fieldValuesHolder, errorCollection, i18n);
                }
            }
        }

        if (errorCollection.hasAnyErrors())
        {
            issue = null;
        }
        return new CreateValidationResult(issue, errorCollection, fieldValuesHolder);
    }

    Map<String, Object> cloneFieldValuesHolder(IssueInputParameters issueInputParameters)
    {
        Map<String, Object> clonedFieldValuesHolder = new HashMap<String, Object>();
        clonedFieldValuesHolder.putAll(issueInputParameters.getFieldValuesHolder());
        clonedFieldValuesHolder.put(IssueFieldConstants.FORM_TOKEN, issueInputParameters.getFormToken());
        return clonedFieldValuesHolder;
    }

    MutableIssue validateAndCreateIssueFromFields(User user, MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, ErrorCollection errorCollection, I18nHelper i18n)
    {
        final FieldScreenRenderer fieldScreenRenderer = getCreateFieldScreenRenderer(user, issue);

        Collection<String> providedFields = issueInputParameters.getProvidedFields();
        // Always default the provided fields to the fields visible on the create screen
        if (providedFields == null)
        {
            providedFields = issueCreationHelperBean.getProvidedFieldNames(issue);
        }
        issueCreationHelperBean.validateCreateIssueFields(new JiraServiceContextImpl(user, errorCollection), providedFields, issue,
                fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, fieldValuesHolder), issueInputParameters, i18n);

        // Update the issues values with the validated values
        if(!errorCollection.hasAnyErrors())
        {
            issueCreationHelperBean.updateIssueFromFieldValuesHolder(fieldScreenRenderer, issue, fieldValuesHolder);
        }

        return issue;
    }

    MutableIssue validateAndSetIssueType(MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, ErrorCollection errorCollection, I18nHelper i18n)
    {
        issueCreationHelperBean.validateIssueType(issue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, fieldValuesHolder), issueInputParameters.getActionParameters(), errorCollection, i18n);
        if (!errorCollection.hasAnyErrors())
        {
            issue.setIssueTypeId(issueInputParameters.getIssueTypeId());
        }
        return issue;
    }

    MutableIssue validateAndSetProject(MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, ErrorCollection errorCollection, I18nHelper i18n)
    {
        issueCreationHelperBean.validateProject(issue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, fieldValuesHolder), issueInputParameters.getActionParameters(), errorCollection, i18n);

        if (!errorCollection.hasAnyErrors())
        {
            issue.setProjectId(issueInputParameters.getProjectId());
        }
        return issue;
    }

    void updateIssueFromFields(FieldScreenRenderer fieldScreenRenderer, MutableIssue issue, User user,
            Map<String, Object> fieldValuesHolder, boolean updateComment, IssueInputParameters issueInputParameters)
    {
        ///JRA-20604, JRADEV-1246: We should not call this method when doing a transition. When doing a transition,
        // the comment is updated using CreateIssueComment
        if (updateComment)
        {
            // This hack is here until the comment field becomes placeable on screens by the users
            updateIssueWithComment(issue, user, fieldValuesHolder);
        }

        // JRADEV-9051: determine whether or not we care about screen checks
        if (!issueInputParameters.skipScreenCheck())
        {
            updateIssueFromFieldsWithScreenCheck(fieldScreenRenderer, issue, fieldValuesHolder, issueInputParameters);
        }
        else
        {
            updateIssueFromFieldsWithoutScreenCheck(fieldScreenRenderer, issue, fieldValuesHolder, issueInputParameters);
        }
    }

    private void updateIssueFromFieldsWithScreenCheck(FieldScreenRenderer fieldScreenRenderer, MutableIssue issue, Map<String, Object> fieldValuesHolder, IssueInputParameters issueInputParameters)
    {
        final boolean retainIssueValues = issueInputParameters.retainExistingValuesWhenParameterNotProvided();
        final boolean onlyValidatePresentFields = issueInputParameters.onlyValidatePresentFieldsWhenRetainingExistingValues();

        for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();
                    if (retainIssueValues && !issueInputParameters.isFieldPresent(orderableField.getId()) && onlyValidatePresentFields)
                    {
                        // JRADEV-9671 - if we skipped the populate-validate step for this field before, then skip it now
                        continue;
                    }
                    orderableField.updateIssue(fieldScreenRenderLayoutItem.getFieldLayoutItem(), issue, fieldValuesHolder);
                }
            }
        }
    }

    private void updateIssueFromFieldsWithoutScreenCheck(FieldScreenRenderer fieldScreenRenderer, MutableIssue issue, Map<String, Object> fieldValuesHolder, IssueInputParameters issueInputParameters)
    {
        final boolean retainIssueValues = issueInputParameters.retainExistingValuesWhenParameterNotProvided();
        final boolean onlyValidatePresentFields = issueInputParameters.onlyValidatePresentFieldsWhenRetainingExistingValues();

        // JRADEV-9051: here, instead of iterating over FieldScreenRenderLayoutItems, we simply need to just iterate over all
        // OrderableFields (which includes CustomFields).
        Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (OrderableField orderableField : orderableFields)
        {
            if (!canEditThisField(issue, orderableField))
            {
                // if we couldn't edit it before, then we don't need to update it now
                continue;
            }

            if (retainIssueValues && !issueInputParameters.isFieldPresent(orderableField.getId()) && onlyValidatePresentFields)
            {
                // JRADEV-9671 - if we skipped the populate-validate step for this field before, then skip it now
                continue;
            }

            FieldLayoutItem fieldLayoutItem = getFieldLayoutItem(issue, orderableField, fieldScreenRenderer);
            orderableField.updateIssue(fieldLayoutItem, issue, fieldValuesHolder);
        }
    }

    /**
     * JRADEV-9051: Try to get the right {@link FieldLayoutItem} for the field. If it doesn't exist - relax. Just return null. The
     * only fields that seem to care about it are fields with Renderers. And they do null checks anyway.
     */
    private FieldLayoutItem getFieldLayoutItem(MutableIssue issue, OrderableField orderableField, FieldScreenRenderer fieldScreenRenderer)
    {
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = getFieldScreenRenderLayoutItem(issue, orderableField, fieldScreenRenderer, false);
        if (fieldScreenRenderLayoutItem != null)
        {
            return fieldScreenRenderLayoutItem.getFieldLayoutItem();
        }
        return null;
    }

    void validateAndPopulateParamsForWorkflowTransition(
            User user,
            MutableIssue issue,
            IssueInputParameters issueInputParameters,
            Map<String, Object> fieldValuesHolder,
            OperationContext operationContext,
            ErrorCollection errorCollection,
            I18nHelper i18n,
            FieldScreenRenderer fieldScreenRenderer,
            int workflowActionId)
    {
        String currentIssueStatus = issue.getStatusObject().getId();
        try
        {
            // JRA-40310: When making a workflow transition, we want to make validations and check permissions in the new status of the issue, not the old one
            String nextIssueStatus = workflowManager.getNextStatusIdForAction(issue, workflowActionId);
            issue.setStatusId(nextIssueStatus);

            validateAndPopulateParams(user, issue, issueInputParameters, fieldValuesHolder, operationContext, errorCollection, i18n, fieldScreenRenderer);
        }
        finally
        {
            issue.setStatusId(currentIssueStatus);
        }
    }

    void validateAndPopulateParams(User user, MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, FieldScreenRenderer fieldScreenRenderer)
    {
        // If a comment has been provided then we will validate it.
        if (issueInputParameters.getCommentValue() != null)
        {
            // validate comments params if there
            OrderableField field = (OrderableField) fieldManager.getField(SystemSearchConstants.forComments().getFieldId());
            field.populateFromParams(fieldValuesHolder, issueInputParameters.getActionParameters());
            field.validateParams(operationContext, errorCollection, i18n, issue, getFieldScreenRendererLayoutItemForField(user, issue, field));
        }

        // JRADEV-9051: determine whether or not we care about screen checks
        if (!issueInputParameters.skipScreenCheck())
        {
            validateAndPopulateParamsWithScreenCheck(issue, issueInputParameters, fieldValuesHolder, operationContext, errorCollection, i18n, fieldScreenRenderer);
        }
        else
        {
            validateAndPopulateParamsWithoutScreenCheck(issue, issueInputParameters, fieldValuesHolder, operationContext, errorCollection, i18n, fieldScreenRenderer);
        }
    }

    private void validateAndPopulateParamsWithScreenCheck(MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, FieldScreenRenderer fieldScreenRenderer)
    {
        final boolean retainIssueValues = issueInputParameters.retainExistingValuesWhenParameterNotProvided();
        final boolean onlyValidatePresentFields = issueInputParameters.onlyValidatePresentFieldsWhenRetainingExistingValues();

        for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    final OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();
                    if (retainIssueValues && !issueInputParameters.isFieldPresent(orderableField.getId()))
                    {
                        // JRADEV-9671 - if IssueInputParameters nominates that only present fields should be validated,
                        // as well as retaining existing values, and this field is not present, then we skip this field
                        if (onlyValidatePresentFields)
                        {
                            continue;
                        }

                        // If they have not provided the field value then we need to populate the field values holder from
                        // the issue, otherwise we can populate it from the action parameters
                        // NOTE: some fields, like the multi-select and checkboxes work such that when the user selects NO values,
                        // the action parameters has nothing submitted for that field. This mechanism is used to "unset" field
                        // values for these fields. Therefore we need a special flag on the input parameters that tells us
                        // how we should treat the "unset" fields.

                        // We need to populate the field values holder from the issue so that if someone is using
                        // the service programatically and just trying to update a subset of the fields that are visible
                        // on the screen we will not over-write or null out the existing values by not having a value
                        // for them in the field values holder. The calls to validate and update expect the field values
                        // holder to have stuff for the field when called. Not having stuff usually results in null'ing
                        // out the fields value.
                        orderableField.populateFromIssue(fieldValuesHolder, issue);
                    }
                    else
                    {
                        orderableField.populateFromParams(fieldValuesHolder, issueInputParameters.getActionParameters());
                    }

                    orderableField.validateParams(operationContext, errorCollection, i18n, issue, fieldScreenRenderLayoutItem);
                }
            }
        }
    }

    private void validateAndPopulateParamsWithoutScreenCheck(MutableIssue issue, IssueInputParameters issueInputParameters, Map<String, Object> fieldValuesHolder, OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, FieldScreenRenderer fieldScreenRenderer)
    {
        final boolean retainIssueValues = issueInputParameters.retainExistingValuesWhenParameterNotProvided();
        final boolean onlyValidatePresentFields = issueInputParameters.onlyValidatePresentFieldsWhenRetainingExistingValues();

        // JRADEV-9051: here, instead of iterating over FieldScreenRenderLayoutItems, we simply need to just iterate over all
        // OrderableFields (which includes CustomFields). For the CustomFields, we need to ensure they are in scope for the issue.
        Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
        for (OrderableField orderableField : orderableFields)
        {
            if (!canEditThisField(issue, orderableField))
            {
                continue;
            }

            // JRADEV-9051: We need an appropriate fieldScreenRenderLayoutItem to use
            FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = getFieldScreenRenderLayoutItem(issue, orderableField, fieldScreenRenderer, true);

            if (retainIssueValues && !issueInputParameters.isFieldPresent(orderableField.getId()))
            {
                // JRADEV-9671 - if IssueInputParameters nominates that only present fields should be validated,
                // as well as retaining existing values, and this field is not present, then we skip this field
                if (onlyValidatePresentFields)
                {
                    continue;
                }

                // JRADEV-9051: if we are on the ResolutionSystemField - we need to make sure that the issue actually has a Resolution
                // before we populateFromIssue
                if (!(orderableField instanceof ResolutionSystemField) || issue.getResolution() != null)
                {
                    // If they have not provided the field value then we need to populate the field values holder from
                    // the issue, otherwise we can populate it from the action parameters
                    // NOTE: some fields, like the multi-select and checkboxes work such that when the user selects NO values,
                    // the action parameters has nothing submitted for that field. This mechanism is used to "unset" field
                    // values for these fields. Therefore we need a special flag on the input parameters that tells us
                    // how we should treat the "unset" fields.

                    // We need to populate the field values holder from the issue so that if someone is using
                    // the service programatically and just trying to update a subset of the fields that are visible
                    // on the screen we will not over-write or null out the existing values by not having a value
                    // for them in the field values holder. The calls to validate and update expect the field values
                    // holder to have stuff for the field when called. Not having stuff usually results in null'ing
                    // out the fields value.
                    orderableField.populateFromIssue(fieldValuesHolder, issue);
                }
            }
            else
            {
                orderableField.populateFromParams(fieldValuesHolder, issueInputParameters.getActionParameters());
            }

            orderableField.validateParams(operationContext, errorCollection, i18n, issue, fieldScreenRenderLayoutItem);
        }
    }

    /**
     * JRADEV-9051: A field can be edited for a given issue if it is not a CustomField, or if it is "in scope" for that issue. This
     * means that there needs to be a relevant {@link com.atlassian.jira.issue.fields.config.FieldConfig} for the custom field.
     *
     * @param issue the issue being updated
     * @param orderableField the field in question
     * @return boolean
     */
    private boolean canEditThisField(MutableIssue issue, OrderableField orderableField)
    {
        if (orderableField instanceof CustomField)
        {
            CustomField customField = (CustomField) orderableField;
            FieldConfig relevantConfig = fieldConfigSchemeManager.getRelevantConfig(issue, customField);
            return relevantConfig != null;
        }
        return true;
    }

    /**
     * JRADEV-9051: Try to get the right {@link FieldScreenRenderLayoutItem} for the field. If it doesn't exist - relax. Just return null. The
     * only fields that seem to care about it are fields with Renderers. And they do null checks anyway.
     */
    private FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem(MutableIssue issue, OrderableField orderableField, FieldScreenRenderer fieldScreenRenderer, boolean useFallback)
    {
        for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    if (orderableField.equals(fieldScreenRenderLayoutItem.getOrderableField()))
                    {
                        return fieldScreenRenderLayoutItem;
                    }
                }
            }
        }

        if (useFallback)
        {
            return getFieldScreenRenderLayoutItemFallback(orderableField);
        }
        return null;
    }

    /**
     * JRADEV-9051: If we are a CustomField, we don't care, but if we are a System Field then return a "fake" layout item that is
     * never "required" (the only check that JIRA system fields seem to do).
     */
    private FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItemFallback(OrderableField orderableField)
    {
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = null;
        boolean needFallback = !(orderableField instanceof CustomField);
        if (needFallback)
        {
            fieldScreenRenderLayoutItem = new NotRequiredFieldScreenRenderLayoutItem();
        }
        return fieldScreenRenderLayoutItem;
    }

    boolean licenseInvalidForIssueCreation(ErrorCollection errorCollection, I18nHelper i18n)
    {
        final SimpleErrorCollection errorCollection1 = new SimpleErrorCollection();
        issueCreationHelperBean.validateLicense(errorCollection1, i18n);
        if (errorCollection1.hasAnyErrors())
        {
            errorCollection.addErrorCollection(errorCollection1);
            return true;
        }
        return false;
    }

    boolean hasPermissionToEdit(User user, Issue issue, I18nHelper i18n, ErrorCollection errors)
    {
        final boolean hasPermission = issueManager.isEditable(issue, user);
        if (!hasPermission)
        {
            errors.addErrorMessage(i18n.getText("editissue.error.no.edit.permission"), user == null ? Reason.NOT_LOGGED_IN : Reason.FORBIDDEN);
        }
        return hasPermission;
    }

    boolean hasPermissionToView(User user, Issue issue, I18nHelper i18n, ErrorCollection errorCollection)
    {
        final boolean hasPermission = permissionManager.hasPermission(Permissions.BROWSE, issue, user);
        if (!hasPermission)
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.issues.no.permission.to.see"), user == null ? Reason.NOT_LOGGED_IN : Reason.FORBIDDEN);
        }
        return hasPermission;
    }

    boolean hasPermissionToCreate(User user, Project project, I18nHelper i18n, ErrorCollection errors)
    {
        final boolean hasPermission = permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        if (!hasPermission)
        {
            errors.addErrorMessage(i18n.getText("createissue.projectnopermission"), user == null ? Reason.NOT_LOGGED_IN : Reason.FORBIDDEN);
        }
        return hasPermission;
    }

    boolean hasPermissionToDelete(User user, Issue issue, I18nHelper i18n, ErrorCollection errors)
    {
        final boolean hasPermission = permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue, user);
        if (!hasPermission)
        {
            errors.addErrorMessage(i18n.getText("admin.errors.issues.no.permission.to.delete"), user == null ? Reason.NOT_LOGGED_IN : Reason.FORBIDDEN);
        }
        return hasPermission;
    }

    MutableIssue constructNewIssue()
    {
        return issueFactory.getIssue();
    }

    MutableIssue copyIssue(MutableIssue issue)
    {
        return issueFactory.getIssue(issue.getGenericValue());
    }

    FieldScreenRenderer getCreateFieldScreenRenderer(User user, Issue issue)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, IssueOperations.CREATE_ISSUE_OPERATION, false);
    }

    FieldScreenRenderer getUpdateFieldScreenRenderer(User user, Issue issue)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, IssueOperations.EDIT_ISSUE_OPERATION, false);
    }

    FieldScreenRenderer getTransitionFieldScreenRenderer(User user, Issue issue, ActionDescriptor actionDescriptor)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, actionDescriptor);
    }

    MutableIssue getIssue(final User user, final MutableIssue issue, final I18nHelper i18n, final ErrorCollection errorCollection)
    {
        if (issue == null)
        {
            // If the issue does not exist then add an error message
            errorCollection.addErrorMessage(i18n.getText("issue.service.issue.wasdeleted"), Reason.NOT_FOUND);            
        }
        else if (hasPermissionToView(user, issue, i18n, errorCollection))
        {
            return issue;
        }

        return null;
    }

    void updateIssueWithComment(final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder)
    {
        final OrderableField commentField = (OrderableField) fieldManager.getField(SystemSearchConstants.forComments().getFieldId());
        final FieldLayoutItem fieldLayoutItem = getFieldScreenRendererLayoutItemForField(user, issue, commentField).getFieldLayoutItem();
        commentField.updateIssue(fieldLayoutItem, issue, fieldValuesHolder);
    }

    FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(User user, Issue issue, OrderableField field)
    {
        final FieldScreenRenderer renderer = fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, IssueOperations.VIEW_ISSUE_OPERATION, false);
        return renderer.getFieldScreenRenderLayoutItem(field);
    }

    ActionDescriptor getActionDescriptor(Issue issue, int actionId)
    {
        final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
        if (workflow == null)
        {
            return null;
        }

        final WorkflowDescriptor descriptor = workflow.getDescriptor();
        if (descriptor == null)
        {
            return null;
        }
        return descriptor.getAction(actionId);
    }

    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }

    Map<String, Object> createAdditionalParameters(final ApplicationUser user, final Map<String, Object> fieldValuesHolder, TransitionOptions transitionOptions, final HistoryMetadata historyMetadata, final String originalAssigneeId)
    {
        final Map<String, Object> additionalParams = new HashMap<String, Object>();
        // put the user info into the additional params
        WorkflowFunctionUtils.populateParamsWithUser(additionalParams, user);

        ///JRA-20604, JRADEV-1246: Need to populate the comment parameters into the additionalParams so they will be available
        //to the workflow actions. Importantly, this means that the event fired by the workflow will contain a comment if provided
        //by the user.
        final CommentSystemField commentSystemField = (CommentSystemField) fieldManager.getOrderableField(IssueFieldConstants.COMMENT);
        commentSystemField.populateAdditionalInputs(fieldValuesHolder, additionalParams);

        additionalParams.putAll(transitionOptions.getWorkflowParams());
        if (historyMetadata != null)
        {
            additionalParams.put(DefaultChangeHistoryManager.HISTORY_METADATA_KEY, historyMetadata);
        }

        additionalParams.put("originalAssigneeId", originalAssigneeId);

        return additionalParams;
    }

    private class SimpleWorkflowProgressAware implements WorkflowProgressAware
    {
        private int action;
        private final ApplicationUser remoteUser;
        private final ErrorCollection errorCollection;
        private final Map additionalInputs;
        private final MutableIssue issue;

        private SimpleWorkflowProgressAware(final int action, final ApplicationUser remoteUser, final ErrorCollection errorCollection, final Map additionalInputs, final MutableIssue issue)
        {
            this.action = action;
            this.remoteUser = remoteUser;
            this.errorCollection = errorCollection;
            this.additionalInputs = additionalInputs;
            this.issue = issue;
        }

        ///CLOVER:OFF
        public User getRemoteUser()
        {
            return ApplicationUsers.toDirectoryUser(remoteUser);
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        @Override
        public ApplicationUser getRemoteApplicationUser()
        {
            return remoteUser;
        }

        public int getAction()
        {
            return action;
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        public void setAction(final int action)
        {
            this.action = action;
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        public void addErrorMessage(final String error)
        {
            errorCollection.addErrorMessage(error);
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        public void addError(final String name, final String error)
        {
            errorCollection.addError(name, error);
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        public Map getAdditionalInputs()
        {
            return additionalInputs;
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        public MutableIssue getIssue()
        {
            return issue;
        }
        ///CLOVER:ON

        ///CLOVER:OFF

        @Override
        public Project getProject()
        {
            return issue.getProjectObject();
        }

        @Override
        public Project getProjectObject()
        {
            return issue.getProjectObject();
        }
        ///CLOVER:ON
    }
}
