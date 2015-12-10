package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.CookieNames;
import com.atlassian.jira.web.util.CheckboxTagSupport;
import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v4.2
 */
public class EditLabels extends AbstractIssueSelectAction
{
    public static final String SEND_NOTIFICATION_CONGLOMERATE_KEY = "labels.send.notification";
    public static final String ISSUE_NOT_FOUND = "issuenotfound";

    private final LabelService labelService;
    private final CustomFieldManager customFieldManager;
    private LabelUtil labelUtil;

    private Long customFieldId;
    private List<String> existingLabels = new ArrayList<String>();
    private Set<String> labels = new LinkedHashSet<String>();
    private boolean noLink;
    private final CheckboxTagSupport sendNotification;

    public EditLabels(LabelService labelService, final CustomFieldManager customFieldManager, final LabelUtil labelUtil)
    {
        this.labelService = labelService;
        this.customFieldManager = customFieldManager;
        this.labelUtil = labelUtil;
        this.sendNotification = new CheckboxTagSupport(sendNotificationCookieValue());
    }

    @Override
    public String doDefault() throws Exception
    {
        try
        {
            return unsafeDoDefault();
        }
        catch (IssueNotFoundException e)
        {
            return ERROR;
        }
        catch (final IssuePermissionException e)
        {
            return ERROR;
        }
    }

    private String unsafeDoDefault() throws Exception
    {
        Issue issue = getIssueObject();
        if (issue == null)
        {
            addErrorMessage(getText("label.service.error.issue.doesnt.exist", getId()));
            return ERROR;
        }

        LabelService.LabelsResult validationResult = getLabelsResult(issue);
        if (validationResult.isValid())
        {
            addToExistingLabels(validationResult.getLabels());
            return INPUT;
        }
        else
        {
            addErrorCollection(validationResult.getErrorCollection());
            return ERROR;
        }
    }

    private LabelService.LabelsResult getLabelsResult(final Issue issue)
    {
        LabelService.LabelsResult labelsResult;
        if (customFieldId == null)
        {
            labelsResult = labelService.getLabels(getLoggedInUser(), issue.getId());
        }
        else
        {
            labelsResult = labelService.getLabels(getLoggedInUser(), issue.getId(), customFieldId);
        }
        return labelsResult;
    }

    public String doViewLinks() throws Exception
    {
        Issue issue = getIssueObject();
        if (issue == null)
        {
            addErrorMessage(getText("label.service.error.issue.doesnt.exist", getId()));
            return ERROR;
        }
        LabelService.LabelsResult labelsResult = getLabelsResult(issue);
        if (labelsResult.isValid())
        {
            addToExistingLabels(labelsResult.getLabels());
            return SUCCESS;
        }
        else
        {
            return ERROR;
        }
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        Issue issue = null;
        try
        {
            issue = getIssueObject();
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_NOT_FOUND;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_NOT_FOUND;
        }

        if (sendNotification.hasChanged())
        {
            setSendNotificationCookie();
        }

        LabelService.SetLabelValidationResult setLabelsValidation;
        if (customFieldId == null)
        {
            setLabelsValidation = labelService.validateSetLabels(getLoggedInUser(), issue.getId(), labels);
        }
        else
        {
            setLabelsValidation = labelService.validateSetLabels(getLoggedInUser(), issue.getId(), customFieldId, labels);
        }

        if (setLabelsValidation.isValid())
        {
            LabelService.LabelsResult setLabelsResult = labelService.setLabels(getLoggedInUser(), setLabelsValidation,
                    sendNotification.postAction(), true);
            if (setLabelsResult.isValid())
            {
                addToExistingLabels(setLabelsResult.getLabels());
                return returnComplete(getViewUrl());
            }
        }

        // JRADEV-2603: don't nuke any labels, just return what the user entered
        existingLabels.addAll(setLabelsValidation.getLabels());
        addErrorCollection(setLabelsValidation.getErrorCollection());
        return ERROR;
    }

    public String getDomId()
    {
        if (customFieldId != null)
        {
            return CustomFieldUtils.CUSTOM_FIELD_PREFIX + customFieldId;
        }
        else
        {
            return IssueFieldConstants.LABELS;
        }
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public String getErrorCollectionKey()
    {
        if (customFieldId == null)
        {
            return IssueFieldConstants.LABELS;
        }
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field != null)
        {
            return field.getId();
        }
        return "";
    }

    public String getFieldName()
    {
        if (customFieldId == null)
        {
            return getText(LabelsSystemField.LABELS_NAME_KEY);
        }
        final CustomField field = customFieldManager.getCustomFieldObject(customFieldId);
        if (field != null)
        {
            return field.getName();
        }
        return "";
    }

    public String getLabelNavigatorUrl(final String label)
    {
        if (customFieldId == null)
        {
            return labelUtil.getLabelJql(getLoggedInUser(), label);
        }
        return labelUtil.getLabelJql(getLoggedInUser(), customFieldId, label);
    }

    public void setCustomFieldId(final Long customFieldId)
    {
        this.customFieldId = customFieldId;
    }

    public List<String> getExistingLabels()
    {
        return existingLabels;
    }

    public void setLabels(final String[] labels)
    {
        this.labels.addAll(Arrays.asList(labels));
    }

    /**
     * Whether the labels should be a link that goes to a search for that label.
     *
     * <p>Required so that when we Ajax replace the labels after editing we can maintain
     * the link/plain text rendering of the labels.</p>
     */
    public boolean isNoLink()
    {
        return noLink;
    }

    public void setNoLink(final boolean noLink)
    {
        this.noLink = noLink;
    }

    public boolean isSendNotification()
    {
        return sendNotification.preAction();
    }

    public void setSendNotification(final boolean doSendNotification)
    {
        sendNotification.postAction(doSendNotification);
    }

    private boolean sendNotificationCookieValue()
    {
        return Boolean.valueOf(getConglomerateCookieValue(CookieNames.JIRA_CONGLOMERATE_COOKIE, SEND_NOTIFICATION_CONGLOMERATE_KEY));
    }

    private void setSendNotificationCookie()
    {
        String value = sendNotification.postAction() ? "true" : "";
        setConglomerateCookieValue(CookieNames.JIRA_CONGLOMERATE_COOKIE, SEND_NOTIFICATION_CONGLOMERATE_KEY, value);
    }

    /**
     * Adds the set of labels to the existingLabels property.
     *
     * @param labels a Set of Label
     */
    private void addToExistingLabels(Set<Label> labels)
    {
        existingLabels.addAll(transform(labels, new LabelToName()));
    }

    /**
     * Functor that maps Label instances to Label names.
     */
    static class LabelToName implements Function<Label, String>
    {
        public String apply(Label label)
        {
            return label.getLabel();
        }
    }
}
