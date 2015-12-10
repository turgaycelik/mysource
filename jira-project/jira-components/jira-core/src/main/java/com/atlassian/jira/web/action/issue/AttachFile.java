package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.FileSize;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.AttachmentExceptionMessages;

import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for rendering the attach file form and converting temporary attachments to real attachments
 * linked to an issue.  This action does not actually handle uploading the attachments themselves.  See
 * {@link com.atlassian.jira.web.action.issue.AttachTemporaryFile} for more details
 *
 * @since v4.2
 */
public class AttachFile extends AbstractCommentableIssue implements OperationContext
{
    private final AttachmentService attachmentService;
    private final AttachmentManager attachmentManager;
    private final IssueUpdater issueUpdater;
    private final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator;
    private long maxSize = Long.MIN_VALUE;
    private String[] filetoconvert;
    private static final String FILETOCONVERT = "filetoconvert";

    public AttachFile(final SubTaskManager subTaskManager, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldManager fieldManager, final ProjectRoleManager projectRoleManager,
            final CommentService commentService, final AttachmentService attachmentService,
            final AttachmentManager attachmentManager, final IssueUpdater issueUpdater,
            final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator, final UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil);
        this.attachmentService = attachmentService;
        this.attachmentManager = attachmentManager;
        this.issueUpdater = issueUpdater;
        this.temporaryAttachmentsMonitorLocator = temporaryAttachmentsMonitorLocator;
    }

    @Override
    public String doDefault() throws Exception
    {
        try
        {
            attachmentService.canCreateAttachments(getJiraServiceContext(), getIssueObject());
        }
        catch (final IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (final IssuePermissionException e)
        {
            return ERROR;
        }
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        try
        {
            attachmentService.canCreateAttachments(getJiraServiceContext(), getIssueObject());
            super.doValidation(); // validate comment
        }
        catch (final IssueNotFoundException ex)
        {
            // Do nothing as error is added above
            return;
        }
        catch (final IssuePermissionException ex)
        {
            // Do nothing as error is added above
            return;
        }

        final List<Long> filesToConvert = getTemporaryFileIdsToConvert();
        if(filesToConvert.isEmpty())
        {
            addError(AttachTemporaryFile.TEMP_FILENAME, getText("attachfile.error.filerequired"));
        }
        else
        {
            for (final Long tempAttachmentId : filesToConvert)
            {
                final TemporaryAttachment temporaryAttachment = getTemporaryAttachment(tempAttachmentId);
                if(temporaryAttachment == null || !temporaryAttachment.getFile().exists())
                {
                    //Display these errors under the upload box as the checkbox will have gone away to were bits go
                    // when they die.
                    addError(AttachTemporaryFile.TEMP_FILENAME, getText("attachment.temporary.id.session.time.out"));
                    break;
                }
            }
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final Collection<ChangeItemBean> changeItemBeans = new ArrayList<ChangeItemBean>();
        final List<Long> fileIdsToConvert = getTemporaryFileIdsToConvert();
        try
        {
            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(false);
            if(temporaryAttachmentsMonitor != null)
            {
                changeItemBeans.addAll(attachmentManager.convertTemporaryAttachments(getLoggedInUser(), getIssueObject(), fileIdsToConvert, temporaryAttachmentsMonitor));
                temporaryAttachmentsMonitor.clearEntriesForFormToken(getFormToken());
            }
            else
            {
                addError(FILETOCONVERT, getText("attachment.temporary.session.time.out"));
                return ERROR;
            }
        }
        catch (final AttachmentException e)
        {
            addError(FILETOCONVERT, AttachmentExceptionMessages.get(e, this));
            return ERROR;
        }

        final IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), getIssue(), EventType.ISSUE_UPDATED_ID, getLoggedInUser());
        issueUpdateBean.setComment(createComment());
        issueUpdateBean.setChangeItems(changeItemBeans);
        issueUpdateBean.setDispatchEvent(true);
        issueUpdateBean.setParams(EasyMap.build("eventsource", IssueEventSource.ACTION));

        issueUpdater.doUpdate(issueUpdateBean, true);

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return getRedirect(redirectToAttachments());
    }

    public String getTargetUrl()
    {
        return isInlineDialogMode() ? redirectToIssue() : redirectToAttachments();
    }

    private String redirectToAttachments()
    {
        return "ManageAttachments.jspa?id=" + getIssue().getLong("id");
    }

    private String redirectToIssue()
    {
        return getViewUrl();
    }


    public String[] getFiletoconvert()
    {
        return filetoconvert;
    }

    public void setFiletoconvert(final String[] filetoconvert)
    {
        this.filetoconvert = filetoconvert;
    }

    public boolean isFileToConvertChecked(final Long tempFileId)
    {
        final List<Long> fileIds = getTemporaryFileIdsToConvert();
        return fileIds.contains(tempFileId);
    }

    public long getMaxSize()
    {
        if (maxSize != Long.MIN_VALUE)
        {
            return maxSize;
        }

        try
        {
            maxSize = Long.parseLong(getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE));
        }
        catch (NumberFormatException e)
        {
            maxSize = -1;
        }
        return maxSize;
    }

    public String getMaxSizePretty()
    {
        final long maxSize = getMaxSize();
        if (maxSize > 0)
        {
            return FileSize.format(maxSize);
        }
        else
        {
            return "Unknown?";
        }
    }

    private List<Long> getTemporaryFileIdsToConvert()
    {
        final String[] strings = getFiletoconvert();
        if(strings == null)
        {
            return Collections.emptyList();
        }
        final List<String> fileIdStrings = Arrays.asList(strings);
        return CollectionUtil.transform(fileIdStrings, new Function<String, Long>()
        {
            public Long get(final String input)
            {
                return Long.parseLong(input);
            }
        });
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }

    private TemporaryAttachment getTemporaryAttachment(final Long temporaryAttachmentId)
    {
        final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(false);
        if (temporaryAttachmentsMonitor != null)
        {
            return temporaryAttachmentsMonitor.getById(temporaryAttachmentId);
        }
        return null;
    }

}
