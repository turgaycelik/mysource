package com.atlassian.jira.bc.issue.label;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.2
 */
public class DefaultLabelService implements LabelService
{
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final LabelManager labelManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final CustomFieldManager customFieldManager;
    private final FieldLayoutManager fieldLayoutManager;

    public DefaultLabelService(final PermissionManager permissionManager, final IssueManager issueManager,
            final LabelManager labelManager, final I18nHelper.BeanFactory beanFactory, final CustomFieldManager customFieldManager,
            final FieldLayoutManager fieldLayoutManager)
    {
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.labelManager = labelManager;
        this.beanFactory = beanFactory;
        this.customFieldManager = customFieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public LabelsResult getLabels(final User user, final Long issueId)
    {
        notNull("issueId", issueId);

        final ErrorCollection errorCollection = validateBrowseIssuePermission(user, issueId);
        if (errorCollection.hasAnyErrors())
        {
            return new LabelsResult(Collections.<Label>emptySet(), errorCollection);
        }
        return new LabelsResult(labelManager.getLabels(issueId), new SimpleErrorCollection());
    }

    @Override
    public LabelsResult getLabels(final User user, final Long issueId, final Long customFieldId)
    {
        notNull("issueId", issueId);
        notNull("customFieldId", customFieldId);

        final ErrorCollection errorCollection = validateBrowseIssuePermission(user, issueId);
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field == null)
        {
            final I18nHelper i18n = beanFactory.getInstance(user);
            errorCollection.addErrorMessage(i18n.getText("label.service.error.custom.field.doesnt.exist", customFieldId));
        }
        if (errorCollection.hasAnyErrors())
        {
            return new LabelsResult(Collections.<Label>emptySet(), errorCollection);
        }
        return new LabelsResult(labelManager.getLabels(issueId, customFieldId), new SimpleErrorCollection());
    }

    @Override
    public SetLabelValidationResult validateSetLabels(final User user, final Long issueId, final Set<String> labels)
    {
        notNull("issueId", issueId);
        notNull("labels", labels);

        final ErrorCollection errors = validateUpdateIssuePermission(user, issueId, null, labels);
        return new SetLabelValidationResult(issueId, null, errors, labels);
    }

    @Override
    public SetLabelValidationResult validateSetLabels(final User user, final Long issueId, final Long customFieldId, final Set<String> labels)
    {
        notNull("issueId", issueId);
        notNull("customFieldId", customFieldId);
        notNull("labels", labels);

        final ErrorCollection errors = validateUpdateIssuePermission(user, issueId, customFieldId, labels);
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field == null)
        {
            final I18nHelper i18n = beanFactory.getInstance(user);
            errors.addErrorMessage(i18n.getText("label.service.error.custom.field.doesnt.exist", customFieldId));
        }
        return new SetLabelValidationResult(issueId, customFieldId, errors, labels);
    }

    @Override
    public LabelsResult setLabels(final User user, final SetLabelValidationResult result, final boolean sendNotification, final boolean causeChangeNotification)
    {
        notNull("result", result);

        if (!result.isValid())
        {
            throw new IllegalArgumentException("Cannot set labels with invalid validation result!");
        }

        Set<Label> newLabels;
        Set<String> cleanLabels = getCleanLabels(result.getLabels());
        if (result.getCustomFieldId() != null)
        {
            newLabels = labelManager.setLabels(user, result.getIssueId(), result.getCustomFieldId(), cleanLabels, sendNotification, causeChangeNotification);
        }
        else
        {
            newLabels = labelManager.setLabels(user, result.getIssueId(), cleanLabels, sendNotification, causeChangeNotification);
        }
        return new LabelsResult(newLabels, new SimpleErrorCollection());
    }

    private Set<String> getCleanLabels(final Set<String> labels)
    {
        final Set<String> cleanLabels = new LinkedHashSet<String>();
        for (String label : labels)
        {
            cleanLabels.add(label.trim());
        }
        return cleanLabels;
    }

    @Override
    public AddLabelValidationResult validateAddLabel(final User user, final Long issueId, final String label)
    {
        notNull("issueId", issueId);
        notNull("label", label);

        final ErrorCollection errors = validateUpdateIssuePermission(user, issueId, null, CollectionBuilder.<String>newBuilder(label).asSet());
        return new AddLabelValidationResult(issueId, null, errors, label);
    }

    @Override
    public AddLabelValidationResult validateAddLabel(final User user, final Long issueId, final Long customFieldId, final String label)
    {
        notNull("issueId", issueId);
        notNull("customFieldId", customFieldId);
        notNull("label", label);

        final ErrorCollection errors = validateUpdateIssuePermission(user, issueId, customFieldId, CollectionBuilder.<String>newBuilder(label).asSet());
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field == null)
        {
            final I18nHelper i18n = beanFactory.getInstance(user);
            errors.addErrorMessage(i18n.getText("label.service.error.custom.field.doesnt.exist", customFieldId));
        }
        return new AddLabelValidationResult(issueId, customFieldId, errors, label);
    }

    @Override
    public LabelsResult addLabel(final User user, final AddLabelValidationResult result, final boolean sendNotification)
    {
        notNull("result", result);

        if (!result.isValid())
        {
            throw new IllegalStateException("Cannot add label with invalid validation result!");
        }

        if (result.getCustomFieldId() != null)
        {
            labelManager.addLabel(user, result.getIssueId(), result.getCustomFieldId(), result.getLabel().trim(), sendNotification);
            return getLabels(user, result.getIssueId(), result.getCustomFieldId());
        }
        else
        {
            labelManager.addLabel(user, result.getIssueId(), result.getLabel(), sendNotification);
            return getLabels(user, result.getIssueId());
        }
    }

    @Override
    public LabelSuggestionResult getSuggestedLabels(final User user, final Long issueId, final String token)
    {
        final ErrorCollection errors = validateForSuggestion(user, token);
        if(errors.hasAnyErrors())
        {
            return new LabelSuggestionResult(Collections.<String>emptySet(), errors);
        }

        return new LabelSuggestionResult(labelManager.getSuggestedLabels(user, issueId, token), new SimpleErrorCollection());
    }

    @Override
    public LabelSuggestionResult getSuggestedLabels(final User user, final Long issueId, final Long customFieldId, final String token)
    {
        notNull("customFieldId", customFieldId);

        final ErrorCollection errors = validateForSuggestion(user, token);
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field == null)
        {
            final I18nHelper i18n = beanFactory.getInstance(user);
            errors.addErrorMessage(i18n.getText("label.service.error.custom.field.doesnt.exist", customFieldId));
        }

        if(errors.hasAnyErrors())
        {
            return new LabelSuggestionResult(Collections.<String>emptySet(), errors);
        }

        return new LabelSuggestionResult(labelManager.getSuggestedLabels(user, issueId, customFieldId, token), new SimpleErrorCollection());
    }

    private ErrorCollection validateForSuggestion(final User user, final String token)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        //check if the token contains any invalid characters.  If it does return this to the client
        if(StringUtils.isNotEmpty(token) && !LabelParser.isValidLabelName(token))
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("label.service.error.label.invalid", token));
        }

        return errors;
    }

    private ErrorCollection validateBrowseIssuePermission(final User user, final Long issueId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final MutableIssue issue = issueManager.getIssueObject(issueId);
        if (issue == null)
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("label.service.error.issue.doesnt.exist", issueId));
            return errors;
        }
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("label.service.error.issue.no.permission", issue.getKey()));
        }
        return errors;
    }

    private ErrorCollection validateUpdateIssuePermission(final User user, final Long issueId, final Long customFieldId, final Set<String> labels)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(user);
        final MutableIssue issue = issueManager.getIssueObject(issueId);
        final String fieldId = customFieldId == null ? IssueFieldConstants.LABELS : CustomFieldUtils.CUSTOM_FIELD_PREFIX + customFieldId;
        if (issue == null)
        {
            errors.addErrorMessage(i18n.getText("label.service.error.issue.doesnt.exist", issueId));
            return errors;
        }
        if (!issueManager.isEditable(issue, user))
        {
            errors.addErrorMessage(i18n.getText("label.service.error.issue.no.edit.permission", issue.getKey()));
        }

        for (String theLabel : labels)
        {
            String label = theLabel.trim();
            if (!LabelParser.isValidLabelName(label))
            {
                errors.addError(fieldId, i18n.getText("label.service.error.label.invalid", label));
            }
            if (label.length() > LabelParser.MAX_LABEL_LENGTH)
            {
                errors.addError(fieldId, i18n.getText("label.service.error.label.toolong", label));
            }
            if (StringUtils.isBlank(label))
            {
                errors.addError(fieldId, i18n.getText("label.service.error.label.blank"));
            }
        }
        
        //validate if the field is required!
        if(!errors.hasAnyErrors())
        {
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
            final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(fieldId);
            if(fieldLayoutItem != null && fieldLayoutItem.isRequired() && labels.isEmpty())
            {
                errors.addError(fieldId, i18n.getText("issue.field.required", i18n.getText(fieldLayoutItem.getOrderableField().getNameKey())));
            }
        }
        return errors;
    }
}
