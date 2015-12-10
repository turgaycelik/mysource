package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.SummarySystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IssueCreationHelperBeanImpl implements IssueCreationHelperBean
{
    private final UserUtil userUtil;
    private final FieldManager fieldManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final JiraLicenseService jiraLicenseService;
    private final JiraContactHelper jiraContactHelper;

    public IssueCreationHelperBeanImpl(UserUtil userUtil,
            FieldManager fieldManager, FieldScreenRendererFactory fieldScreenRendererFactory, final JiraLicenseService jiraLicenseService, JiraContactHelper jiraContactHelper)
    {
        this.userUtil = userUtil;
        this.fieldManager = fieldManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.jiraContactHelper = jiraContactHelper;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
    }

    @Override
    public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer,
            final OperationContext operationContext, final Map<String, String[]> actionParams, final boolean applyDefaults, final I18nHelper i18n)
    {
        validateCreateIssueFields(jiraServiceContext, providedFields, issueObject, fieldScreenRenderer, operationContext, actionParams, applyDefaults, false, i18n);
    }

    @Override
    public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer, final OperationContext operationContext, final IssueInputParameters issueInputParameters, final I18nHelper i18n)
    {
        validateCreateIssueFields(jiraServiceContext, providedFields, issueObject, fieldScreenRenderer, operationContext,
                issueInputParameters.getActionParameters(), issueInputParameters.applyDefaultValuesWhenParameterNotProvided(),
                issueInputParameters.skipScreenCheck(), i18n);
    }

    protected void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer,
            final OperationContext operationContext, final Map<String, String[]> actionParams, final boolean applyDefaults, final boolean skipScreenCheck, final I18nHelper i18n)
    {
        ErrorCollection errors = jiraServiceContext.getErrorCollection();
        FieldLayout fieldLayout = fieldScreenRenderer.getFieldLayout();
        List<FieldLayoutItem> visibleLayoutItems = fieldLayout.getVisibleLayoutItems(issueObject.getProjectObject(), Lists.newArrayList(issueObject.getIssueTypeObject().getId()));
        for (final FieldLayoutItem fieldLayoutItem : visibleLayoutItems)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            // A hack to get around issue type not being shown  - issue type is always shown as it is always required.
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
            {
                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = fieldScreenRenderer.getFieldScreenRenderLayoutItem(orderableField);
                // Test if the field has been shown to the user (i.e. test that it appears on the field screen and was rendered for the user) - otherwise there is no need to validate it
                final boolean wasShown = skipScreenCheck || (fieldScreenRenderLayoutItem != null && fieldScreenRenderLayoutItem.isShow(issueObject));

                if (wasShown && providedFields.contains(orderableField.getId()))
                {
                    if (!orderableField.hasParam(actionParams) && applyDefaults)
                    {
                        orderableField.populateDefaults(operationContext.getFieldValuesHolder(), issueObject);
                    }
                    else
                    {
                        orderableField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
                    }
                    try
                    {
                        orderableField.validateParams(operationContext, errors, i18n, issueObject, fieldScreenRenderLayoutItem);
                    }
                    catch (FieldValidationException e)
                    {
                        errors.addError(orderableField.getId(), e.getMessage(), Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    // The default resolution should not be set on issue creation
                    if (!IssueFieldConstants.RESOLUTION.equals(orderableField.getId()))
                    {
                        // If the field has not been shown then let it populate the params with 'default' values
                        orderableField.populateDefaults(operationContext.getFieldValuesHolder(), issueObject);
                        ErrorCollection errorCollection = new SimpleErrorCollection();
                        // Validate the parameter. In theory as the field places a default value itself the value should be valid, however, a check for
                        // 'requireability' still has to be made.
                        try
                        {
                            orderableField.validateParams(operationContext, errorCollection, i18n, issueObject, fieldScreenRenderLayoutItem);
                        }
                        catch (FieldValidationException e)
                        {
                            errorCollection.addError(orderableField.getId(), e.getMessage());
                        }
                        if (errorCollection.getErrors() != null && !errorCollection.getErrors().isEmpty())
                        {
                            // The field has reported errors but is not rendered on the screen - report errors as error messages
                            for (final String s : errorCollection.getErrors().values())
                            {
                                String result;
                                if (orderableField instanceof CustomField)
                                {
                                    result = orderableField.getName();
                                }
                                else
                                {
                                    result = i18n.getText(orderableField.getNameKey());
                                }
                                errors.addErrorMessage(result + ": " + s);
                            }
                        }
                        errors.addErrorMessages(errorCollection.getErrorMessages());
                        errors.addReasons(errorCollection.getReasons());
                    }
                }
            }
        }
    }

    public void validateLicense(final ErrorCollection errors,
                                final I18nHelper i18n)
    {
        String contactLink = jiraContactHelper.getAdministratorContactMessage(i18n);
        final LicenseDetails licenseDetails = getLicenseDetails();
        if (!licenseDetails.isLicenseSet())
        {
            errors.addErrorMessage(i18n.getText(
                "createissue.error.invalid.license", contactLink), Reason.FORBIDDEN);
        }
        else if (licenseDetails.isExpired())
        {
            if (licenseDetails.isDataCenter() || licenseDetails.isEnterpriseLicenseAgreement())
            {
                errors.addErrorMessage(i18n.getText(
                    "createissue.error.enterprise.license.expired", contactLink), Reason.FORBIDDEN);
            }
            else
            {
                // evaluation period has expired
                errors.addErrorMessage(i18n.getText(
                    "createissue.error.license.expired", contactLink), Reason.FORBIDDEN);
            }
        }
        else if(userUtil.hasExceededUserLimit())
        {
            errors.addErrorMessage(i18n.getText(
                "createissue.error.license.user.limit.exceeded", contactLink), Reason.FORBIDDEN);
        }
    }

    //used for testing
    LicenseDetails getLicenseDetails()
    {
        return jiraLicenseService.getLicense();
    }

    @Override
    public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final User remoteUser, final MutableIssue issueObject, final Map fieldValuesHolder)
    {
        updateIssueFromFieldValuesHolder(fieldScreenRenderer, issueObject, fieldValuesHolder);
    }

    @Override
    public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issueObject, final Map fieldValuesHolder)
    {
        FieldLayout fieldLayout = fieldScreenRenderer.getFieldLayout();
        List<FieldLayoutItem> visibleLayoutItems = fieldLayout.getVisibleLayoutItems(issueObject.getProjectObject(), Lists.newArrayList(issueObject.getIssueTypeObject().getId()));
        for (final FieldLayoutItem fieldLayoutItem : visibleLayoutItems)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            // A hack to get arround issue type not being shown  - issue type is always shown as it is always required.
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
            {
                // Update the issue with needed values
                orderableField.updateIssue(fieldLayoutItem, issueObject, fieldValuesHolder);
            }
        }
    }

    /**
     * Create a field screen renderer
     *
     * @param remoteUser
     * @param issueObject - with issue type and project
     */
    @Override
    public FieldScreenRenderer createFieldScreenRenderer(final User remoteUser, final Issue issueObject)
    {
        return createFieldScreenRenderer(issueObject);
    }

    @Override
    public FieldScreenRenderer createFieldScreenRenderer(final Issue issueObject)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(issueObject, IssueOperations.CREATE_ISSUE_OPERATION);
    }

    @Override
    public List<String> getProvidedFieldNames(final User remoteUser, final Issue issueObject)
    {
        return getProvidedFieldNames(issueObject);
    }

    @Override
    public List<String> getProvidedFieldNames(final Issue issueObject)
    {
        List providedFieldNames = new ArrayList();
        FieldScreenRenderer fieldScreenRenderer = createFieldScreenRenderer(issueObject);
        List<FieldLayoutItem> visibleLayoutItems = fieldScreenRenderer.getFieldLayout().getVisibleLayoutItems(issueObject.getProjectObject(), Lists.newArrayList(issueObject.getIssueTypeObject().getId()));
        for (final FieldLayoutItem fieldLayoutItem : visibleLayoutItems)
        {
            String fieldId = fieldLayoutItem.getOrderableField().getId();
            providedFieldNames.add(fieldId);
        }
        return providedFieldNames;
    }

    public List<OrderableField> getFieldsForCreate(User user, Issue issueObject)
    {
        final List<OrderableField> fields = new ArrayList<OrderableField>();

        FieldScreenRenderer fieldScreenRenderer = createFieldScreenRenderer(issueObject);

        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                if (fieldScreenRenderLayoutItem.isShow(issueObject))
                {
                    fields.add(fieldScreenRenderLayoutItem.getOrderableField());
                }
            }
        }
        return fields;
    }

    public void validateProject(Issue issue, OperationContext operationContext, Map actionParams, final ErrorCollection errors,
                                final I18nHelper i18n)
    {
        // Check that the project selected is a valid one
        ProjectSystemField projectField = (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
        projectField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
        projectField.validateParams(operationContext, errors, i18n, issue, null);
    }

    public void validateIssueType(Issue issue, OperationContext operationContext, Map actionParams, final ErrorCollection errors,
                                  final I18nHelper i18n)
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
        issueTypeField.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
        issueTypeField.validateParams(operationContext, errors, i18n, issue, null);
    }

    public void validateSummary(Issue issue, OperationContext operationContext, Map actionParams, final ErrorCollection errors,
                                  final I18nHelper i18n)
    {
        final SummarySystemField field = (SummarySystemField) getField(IssueFieldConstants.SUMMARY);
        field.populateFromParams(operationContext.getFieldValuesHolder(), actionParams);
        field.validateParams(operationContext, errors, i18n, issue, null);
    }

    public Field getField(String id)
    {
        return fieldManager.getField(id);
    }
}
