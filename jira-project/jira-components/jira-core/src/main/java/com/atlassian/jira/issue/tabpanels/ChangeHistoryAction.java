package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.renderer.HistoryMetadataRenderHelper;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.util.JiraDurationUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This class is the wrapper around the ChangeHistory object and is used when displaying change history in the View
 * Issue page, on the 'Change History' tab panel.
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class ChangeHistoryAction extends AbstractIssueAction
{
    private final ChangeHistory changeHistory;
    private final boolean showHeader;
    private final AttachmentManager attachmentManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final CustomFieldManager customFieldManager;
    private final DateTimeFormatter formatter;
    private final Issue issue;
    private final DateTimeFieldChangeLogHelper changeLogHelper;
    private final HistoryMetadata historyMetadata;
    private final HistoryMetadataRenderHelper historyMetadataRenderHelper;

    public ChangeHistoryAction(
                    IssueTabPanelModuleDescriptor descriptor,
                    ChangeHistory changeHistory,
                    boolean showHeader,
                    AttachmentManager attachmentManager,
                    JiraDurationUtils jiraDurationUtils,
                    CustomFieldManager customFieldManager,
                    DateTimeFormatter formatter,
                    Issue issue,
                    DateTimeFieldChangeLogHelper changeLogHelper,
                    HistoryMetadata historyMetadata,
                    HistoryMetadataRenderHelper historyMetadataRenderHelper)
    {
        super(descriptor);
        this.changeHistory = changeHistory;
        this.showHeader = showHeader;
        this.attachmentManager = attachmentManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.customFieldManager = customFieldManager;
        this.formatter = formatter;
        this.issue = issue;
        this.changeLogHelper = changeLogHelper;
        this.historyMetadata = historyMetadata;
        this.historyMetadataRenderHelper = historyMetadataRenderHelper;
    }

    public Date getTimePerformed()
    {
        return getCreatedDate();
    }

    protected void populateVelocityParams(Map params)
    {
        params.put("changehistory", this);
        params.put("metadataHelper", historyMetadataRenderHelper);
        params.put("stringUtils", new StringUtils());
    }

    //-------------------------------------------------------------------------------- Methods used by velocity template
    public boolean isShowHeader()
    {
        return showHeader;
    }

    public Long getId()
    {
        return changeHistory.getId();
    }

    public String getAuthor()
    {
        return changeHistory.getAuthorKey();
    }

    public String getFullName()
    {
        return changeHistory.getAuthorDisplayName();
    }

    public Timestamp getCreatedDate()
    {
        return changeHistory.getTimePerformed();
    }

    public String getCreatedDateHtml()
    {
        return escapeHtml(formatter.forLoggedInUser().format(getCreatedDate()));
    }

    public String getCreatedDateIso8601Html()
    {
        return escapeHtml(formatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(getCreatedDate()));
    }

    public String getComment()
    {
        return changeHistory.getComment();
    }

    public String getLevel()
    {
        return changeHistory.getLevel();
    }

    public List getChangeItems()
    {
        return changeHistory.getChangeItems();
    }

    public boolean isAttachmentValid(String fileId)
    {
        try
        {
            return (fileId != null && attachmentManager.getAttachment(new Long(fileId)) != null);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean isDateField(final String fieldName)
    {
        if ("duedate".equals(fieldName.toLowerCase()))
        {
            return true;
        }
        CustomField customField = findDateCustomField(fieldName);
        return customField != null;

    }

    public String formatDateValue(final String value, final String string, final String fieldName)
    {
        if ("duedate".equals(fieldName.toLowerCase()))
        {
           return changeLogHelper.renderChangeHistoryValueDate(value, string);
        }
        CustomField customField = findDateCustomField(fieldName);

        if (customField == null) return string;

        if (customField.getCustomFieldType() instanceof DateTimeCFType)
        {
            return changeLogHelper.renderChangeHistoryValueDateTime(value, string);
        }
        else /*is DateCFType*/
        {
            return changeLogHelper.renderChangeHistoryValueDate(value, string);
        }
    }

    public String formatValueStringForDisplay(final String value)
    {
        // JRADEV-16964 strip surrounding brackets if the value has them, to prevent unsightly double-bracketing.
        // If the value is a literal empty pair of brackets for some reason, leave them as is.
        if (value == null)
        {
            return "";
        }
        if (value.startsWith("[") && value.endsWith("]"))
        {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private CustomField findDateCustomField(final String fieldName)
    {
        List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
        final CustomField customField;
        try
        {
            customField = Iterables.find(customFieldObjects, new Predicate<CustomField>()
            {
                @Override
                public boolean apply(@Nullable CustomField customField)
                {
                    return customField.getName().equals(fieldName) &&
                            ((customField.getCustomFieldType() instanceof DateCFType)
                            || (customField.getCustomFieldType() instanceof DateTimeCFType));
                }
            });
        }
        catch (NoSuchElementException ex)
        {
            return null;
        }
        return customField;
    }

    public String getPrettyDuration(String duration)
    {
        return jiraDurationUtils.getFormattedDuration(new Long(duration));
    }

    public HistoryMetadata getHistoryMetadata() {
        return historyMetadata;
    }
}
