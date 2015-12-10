package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkDisplayHelper;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This action is only used by jelly. It is not shown on the user interface.
 * 
 * @deprecated Since v5.0.
 */
public class LinkExistingIssue extends AbstractIssueLinkAction
{
    private String[] linkKey;
    private String linkDesc;
    private Collection<String> linkDescs;

    private IssueLinkType issueLinkType;

    final private List<MutableIssue> issues = new ArrayList<MutableIssue>();
    private final IssueLinkManager issueLinkManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkService issueLinkService;
    private final IssueLinkDisplayHelper issueLinkDisplayHelper;
    private final UserHistoryManager userHistoryManager;

    public LinkExistingIssue(
            final IssueLinkManager issueLinkManager,
            final IssueLinkTypeManager issueLinkTypeManager,
            final SubTaskManager subTaskManager,
            final FieldManager fieldManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final ProjectRoleManager projectRoleManager,
            final CommentService commentService,
            final UserHistoryManager userHistoryManager,
            final IssueLinkService issueLinkService,
            final UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil, null, null);
        this.issueLinkManager = issueLinkManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkService = issueLinkService;
        this.userHistoryManager = userHistoryManager;
        this.issueLinkDisplayHelper = new IssueLinkDisplayHelper(userHistoryManager, getLoggedInUser());

        ComponentAccessor.getComponent(WebResourceManager.class).requireResource("jira.webresources:jira-fields");
    }

    @Override
    public boolean isValidToView()
    {
        return super.isValidToView()
                && !getLinkDescs().isEmpty();
    }

    protected void doValidation()
    {
        try
        {
            // Validate comment and permissions
            super.doValidation();

            if (linkKey != null && linkKey.length > 0)
            {
                for (String key : linkKey)
                {
                    final MutableIssue issue = ComponentAccessor.getComponent(IssueManager.class).getIssueObject(key);
                    if (issue == null)
                    {
                        addError("linkKey", getText("linkissue.error.notexist", key));
                    }
                    else if (key.equals(getIssue().getString("key")))
                    {
                        addError("linkKey", getText("linkissue.error.selflink"));
                    }
                    else
                    {
                        issues.add(issue);
                    }

                }
            }
            else
            {
                addError("linkKey", getText("linkissue.error.keyrequired"));
            }

            if (!getLinkDescs().contains(linkDesc))
            {
                addError("linkDesc", getText("linkissue.error.invalidlinkdesc"));
            }
            else if (getIssueLinkType().isSystemLinkType())
            {
                // Check that the chosen link type is not a system link type.
                // This should not happen - the system should not present a user with the description
                // of a system link type to chose
                addError("linkDesc", getText("linkissue.error.systemlink"));
            }
        }
        catch (IssueNotFoundException e)
        {
            // Do nothing as error added above
        }
        catch (IssuePermissionException e)
        {
            // Do nothing as error added above
        }
    }

    public String doDefault() throws Exception
    {
        try
        {
            getIssueObject();
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        return super.doDefault();

    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        IssueLinkType linkType = getIssueLinkType();

        for (final MutableIssue issue : issues)
        {
            linkIssue(linkType, issue);
        }

        createComment();

        return returnComplete(getRedirectUrl());
    }

    private void linkIssue(IssueLinkType linkType, MutableIssue destinationIssue)
    {
        try
        {
            if (linkDesc.equals(linkType.getOutward()))
            {
                issueLinkManager.createIssueLink(getIssue().getLong("id"), destinationIssue.getId(), linkType.getId(), null, getLoggedInUser());
            }
            else
            {
                issueLinkManager.createIssueLink(destinationIssue.getId(), getIssue().getLong("id"), linkType.getId(), null, getLoggedInUser());
            }
            userHistoryManager.addItemToHistory(UserHistoryItem.ISSUELINKTYPE, getLoggedInUser(), String.valueOf(linkType.getId()), linkDesc);
        }
        catch (Exception e)
        {
            log.error("Error occurred creating link: " + e, e);
            addErrorMessage(getText("admin.errors.issues.an.error.occured", e));
        }
    }

    private IssueLinkType getIssueLinkType()
    {
        if (issueLinkType == null)
        {
            for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes())
            {
                if (linkDesc.equals(linkType.getOutward()) || linkDesc.equals(linkType.getInward()))
                {
                    issueLinkType = linkType;
                    break;
                }
            }
        }

        return issueLinkType;
    }

    @SuppressWarnings("unused")
    public String[] getLinkKey()
    {
        return linkKey;
    }

    @SuppressWarnings("unused")
    public void setLinkKey(String[] linkKey)
    {
        this.linkKey = linkKey;
    }

    @SuppressWarnings("unused")
    public String getLinkDesc()
    {
        return linkDesc;
    }

    @SuppressWarnings("unused")
    public void setLinkDesc(String linkDesc)
    {
        this.linkDesc = linkDesc;
    }

    public Collection getLinkDescs()
    {
        if (linkDescs == null)
        {
            linkDescs = issueLinkDisplayHelper.getSortedIssueLinkTypes(issueLinkService.getIssueLinkTypes());
        }

        return linkDescs;
    }

    @SuppressWarnings("unused")
    public String getLastUsedLinkType()
    {
        return issueLinkDisplayHelper.getLastUsedLinkType();
    }

    @SuppressWarnings("unused")
    public List<MutableIssue> getCurrentValue()
    {
        return issues;
    }
}
