package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Date;

public class ManageAttachments extends AbstractCommentableIssue
{
    private static final String ATTACH_FILE_ISSUE_OPERATION_KEY = "com.atlassian.jira.plugin.system.issueoperations:attach-file";
    private static final String ATTACH_SCREENSHOT_ISSUE_OPERATION_KEY = "com.atlassian.jira.plugin.system.issueoperations:attach-screenshot";

    private final AttachmentService attachmentService;
    private final PluginAccessor pluginAccessor;

    private Collection<GenericValue> affectedVersions;
    private Collection<GenericValue> components;
    private Collection<GenericValue> fixVersions;
    private Issue parentIssueObject = null;

    public ManageAttachments(SubTaskManager subTaskManager, AttachmentService attachmentService,
            PluginAccessor pluginAccessor, FieldScreenRendererFactory fieldScreenRendererFactory,
            FieldManager fieldManager, ProjectRoleManager projectRoleManager, CommentService commentService,
            UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil);
        this.attachmentService = attachmentService;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    protected void doValidation()
    {
    }

    @Override
    protected String doExecute() throws Exception
    {
        final Issue issue;
        try
        {
            issue = getIssueObject();
        }
        catch (final IssueNotFoundException ex)
        {
            addErrorMessage(getText("admin.errors.issues.issue.does.not.exist"));
            return ISSUE_NOT_FOUND_RESULT;
        }
        catch (final IssuePermissionException ex)
        {
            addErrorMessage(getText("admin.errors.issues.no.browse.permission"));
            return PERMISSION_VIOLATION_RESULT;
        }

        return super.doExecute();
    }

    public boolean isScreenshotAttachable()
    {
        /*
          Test whether the user is on Windows or OSX. No other operating system (that I know about) is able to paste
          images into the clipboard such that they are available to a Java applet. So we should not make the
          'attach screenshot' link available to users that are not on Windows. Note, that here we are testing
          whether the user's browser is running on Windows - NOT whether the app server that runs JIRA webapp is
          running on Windows.

          Also check whether the applet is enabled

          JRA-12403 - check that the attach-file issue operation plugin is visible
        */
        return isIssueOperationShowable(ATTACH_SCREENSHOT_ISSUE_OPERATION_KEY) &&
                attachmentService.canAttachScreenshots(getJiraServiceContext(), getIssueObject());
    }

    public boolean isAttachable()
    {
        //JRA-12409 - you can only attach a file to the issue if the user has permission to create an attachment and the
        // issue is in an editable workflow state.
        // JRA-12403 - check that the attach-file issue operation plugin is visible
        return isIssueOperationShowable(ATTACH_FILE_ISSUE_OPERATION_KEY) &&
                attachmentService.canCreateAttachments(getJiraServiceContext(), getIssueObject());
    }

    public Collection<GenericValue> getComponents() throws Exception
    {
        if (components == null)
        {
            components = getIssueObject().getComponents();
        }
        return components;
    }

    public Collection<GenericValue> getAffectedVersions() throws Exception
    {
        if (affectedVersions == null)
        {
            affectedVersions = Collections2.transform(getIssueObject().getAffectedVersions(), new Function<Version, GenericValue>()
            {
                @Override
                public GenericValue apply(Version version)
                {
                    return version.getGenericValue();
                }
            });
        }
        return affectedVersions;
    }

    public Collection<GenericValue> getFixVersions() throws Exception
    {
        if (fixVersions == null)
        {
            fixVersions = Collections2.transform(getIssueObject().getFixVersions(), new Function<Version, GenericValue>()
            {
                @Override
                public GenericValue apply(Version version)
                {
                    return version.getGenericValue();
                }
            });
        }
        return fixVersions;
    }

    public boolean isHasDeleteAttachmentPermission(Long attachmentId)
    {
        // Do not call this with the action as the error collection, otherwise you will get permission errors for
        // those attachments you do not have permission to delete
        JiraServiceContext context = new JiraServiceContextImpl(getLoggedInUser(), new SimpleErrorCollection());
        return attachmentService.canDeleteAttachment(context, attachmentId);
    }

    /**
     * Returns the parent of the current {@link Issue}
     *
     * @return the parent issue object
     */
    public Issue getParentIssueObject()
    {
        if (isSubTask())
        {
            if (parentIssueObject == null)
            {
                final Issue issue = getIssueObject();
                if (issue.isSubTask() && issue.getParentObject() != null)
                {
                    parentIssueObject = issue.getParentObject();
                }
            }
        }

        return parentIssueObject;
    }

    private boolean isIssueOperationShowable(String issueOperationKey)
    {
        ModuleDescriptor moduleDescriptor = pluginAccessor.getEnabledPluginModule(issueOperationKey);
        return moduleDescriptor != null;
    }

    public boolean getZipSupport()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

    public DateTimeFormatter getIso8601Formatter()
    {
        return getDateTimeFormatter().withStyle(DateTimeStyle.ISO_8601_DATE_TIME);
    }

}
