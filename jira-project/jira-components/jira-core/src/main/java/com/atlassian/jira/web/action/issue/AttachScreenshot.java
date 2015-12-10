package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.SecureUserTokenManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.AttachmentExceptionMessages;
import com.atlassian.jira.web.util.WebAttachmentManager;
import org.apache.commons.lang.StringUtils;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Webwork action for popping up the attach screenshot applet and attaching screenshots to an issue.
 *
 * @since v4.2
 */
public class AttachScreenshot extends AbstractCommentableIssue implements OperationContext
{
    private static final String SCREENSHOT_PREFIX = "screenshot-";
    private static final Pattern SHOT_NUMBER_REGEX = Pattern.compile("screenshot-(\\d+)");

    private final WebAttachmentManager webAttachmentManager;
    private final IssueUpdater issueUpdater;
    private final AttachmentService attachmentService;
    private final SecureUserTokenManager secureUserTokenManager;
    private final JiraAuthenticationContext authenticationContext;

    private String secureToken;
    private User secureUser;

    public AttachScreenshot(final SubTaskManager subTaskManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final FieldManager fieldManager,
            final ProjectRoleManager projectRoleManager, final CommentService commentService,
            final WebAttachmentManager webAttachmentManager, final IssueUpdater issueUpdater, final AttachmentService attachmentService,
            final SecureUserTokenManager secureUserTokenManager, final JiraAuthenticationContext authenticationContext, final UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil);
        this.webAttachmentManager = webAttachmentManager;
        this.issueUpdater = issueUpdater;
        this.attachmentService = attachmentService;
        this.secureUserTokenManager = secureUserTokenManager;
        this.authenticationContext = authenticationContext;
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
        //need to set the user from the secure token in the auth context before validation since some
        //permission checks rely on it!
        final User originalUser = authenticationContext.getLoggedInUser();
        authenticationContext.setLoggedInUser(getLoggedInUser());
        try
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

            boolean atLeastOneFile = false;
            try
            {
                final boolean exists = webAttachmentManager.validateAttachmentIfExists(ServletActionContext.getMultiPartRequest(), "filename.1", false);
                atLeastOneFile = atLeastOneFile || exists;
            }
            catch (final AttachmentException e)
            {
                addErrorMessage(AttachmentExceptionMessages.get(e, this));
            }

            if (invalidInput())
            {
                addErrorMessage(getText("issue.field.attachment.lostduetoerrors"));
            }
            else if (!atLeastOneFile)
            {
                addError("filename.1", getText("attachfile.error.filerequired"));
            }
        }
        finally
        {
            authenticationContext.setLoggedInUser(originalUser);
        }
    }


    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        //need to set the user from the secure token in the auth context before validation since some
        //permission checks rely on it!
        final User originalUser = authenticationContext.getLoggedInUser();
        authenticationContext.setLoggedInUser(getLoggedInUser());
        try
        {
            final Collection<ChangeItemBean> changeItemBeans = new ArrayList<ChangeItemBean>();
            try
            {
                final ChangeItemBean changeItemBean = webAttachmentManager.createAttachment(ServletActionContext.getMultiPartRequest(), getLoggedInUser(), getIssueObject(), "filename.1", null);
                if (changeItemBean != null)
                {
                    changeItemBeans.add(changeItemBean);
                }
            }
            catch (final AttachmentException e)
            {
                addError("filename.1", AttachmentExceptionMessages.get(e, this));
                return ERROR;
            }


            final IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), getIssue(), EventType.ISSUE_UPDATED_ID, getLoggedInUser());
            issueUpdateBean.setComment(createComment());
            issueUpdateBean.setChangeItems(changeItemBeans);
            issueUpdateBean.setDispatchEvent(true);
            issueUpdateBean.setParams(MapBuilder.singletonMap("eventsource", IssueEventSource.ACTION));

            issueUpdater.doUpdate(issueUpdateBean, true);

            return NONE;
        }
        finally
        {
            authenticationContext.setLoggedInUser(originalUser);
        }

    }

    @Override
    public String getReturnUrl()
    {
        final String returnUrl = super.getReturnUrl();
        return StringUtils.isNotBlank(returnUrl) ? returnUrl : insertContextPath("/browse/" + getIssueObject().getKey());
    }

    public boolean isAbleToAttach()
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.CREATE_ATTACHMENT, getIssueObject(), getLoggedInUser());
    }

    public String encode(final String text)
    {
        return JSONEscaper.escape(text);
    }

    public String getNextScreenshotName()
    {
        return SCREENSHOT_PREFIX + getNextScreenshotNumber();
    }

    public String getNewUserToken()
    {
        return secureUserTokenManager.generateToken(getLoggedInUser(), SecureUserTokenManager.TokenType.SCREENSHOT);
    }

    public int getNextScreenshotNumber()
    {
        int last = 0;
        for (final Attachment attachment : getIssueObject().getAttachments())
        {
            final Matcher matcher = SHOT_NUMBER_REGEX.matcher(attachment.getFilename());
            if (matcher.find())
            {
                try
                {
                    last = Math.max(last, Integer.parseInt(matcher.group(1)));
                }
                catch (RuntimeException impossible)
                {
                    log.info("problem parsing screenshot number in " + attachment.getFilename(), impossible);
                }
            }
        }
        return last + 1;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }

    public String getSecureToken()
    {
        return secureToken;
    }

    public void setSecureToken(String secureToken)
    {
        this.secureToken = secureToken;
    }

    @Override
    public User getLoggedInUser()
    {
        return getSecureUser();
    }

    private User getSecureUser()
    {
        //if the secureUser hasn't been set yet and we have a token resolve the token to a user.
        if(secureUser == null && secureToken != null)
        {
            secureUser = secureUserTokenManager.useToken(secureToken, SecureUserTokenManager.TokenType.SCREENSHOT);
        }

        if(secureUser == null)
        {
            //looks like we didn't resolve a token to a secureUser.  Let's use the logged in user from seraph.
            return super.getLoggedInUser();
        }
        else
        {
            //got a user that was resolved from a token.  Pretend we're that person.
            return secureUser;
        }
    }
}


