package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class DeleteGroup extends IssueActionSupport
{
    String name;
    String swapGroup;
    boolean confirm;

    private final SubscriptionManager subscriptionManager;
    private final SearchRequestService searchRequestService;
    private final GroupService groupService;
    private final GroupManager groupManager;
    private final CrowdService crowdService;

    public DeleteGroup(IssueManager issueManager, CustomFieldManager customFieldManager, AttachmentManager attachmentManager,
            ProjectManager projectManager, PermissionManager permissionManager, VersionManager versionManager,
            SubscriptionManager subscriptionManager, SearchRequestService searchRequestService, GroupService groupService,
            UserIssueHistoryManager userHistoryManager, TimeTrackingConfiguration timeTrackingConfiguration, GroupManager groupManager, CrowdService crowdService)
    {
        super(issueManager, customFieldManager, attachmentManager, projectManager, permissionManager, versionManager, userHistoryManager, timeTrackingConfiguration);
        this.subscriptionManager = subscriptionManager;
        this.searchRequestService = searchRequestService;
        this.groupService = groupService;
        this.groupManager = groupManager;
        this.crowdService = crowdService;
    }

    public String doDefault() throws Exception
    {
        groupService.isAdminDeletingSysAdminGroup(getJiraServiceContext(), name);
        groupService.areOnlyGroupsGrantingUserAdminPermissions(getJiraServiceContext(), EasyList.build(name));
        return super.doDefault();
    }

    protected void doValidation()
    {
        groupService.validateDelete(getJiraServiceContext(), name, swapGroup);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            groupService.delete(getJiraServiceContext(), name, swapGroup);
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect("GroupBrowser.jspa");
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public String getSwapGroup()
    {
        return swapGroup;
    }

    public void setSwapGroup(String swapGroup)
    {
        this.swapGroup = swapGroup;
    }

    public long getMatchingCommentsAndWorklogsCount() throws GenericEntityException
    {
        return groupService.getCommentsAndWorklogsGuardedByGroupCount(name);
    }

    /**
     * @return all other groups except this one
     */
    public Collection getOtherGroups()
    {
        List<String> otherGroups = new ArrayList<String>();

        try
        {
            Collection<Group> groups = groupManager.getAllGroups();

            for (final Group group : groups)
            {
                if (!group.getName().equals(name))
                {
                    otherGroups.add(group.getName());
                }
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.groups.error.occured.getting.other.groups"));
        }

        return otherGroups;
    }


    public boolean hasSubscriptions()
    {
        try
        {
            List<GenericValue> subList = subscriptionManager.getAllSubscriptions();
            for (final GenericValue gv : subList)
            {
                if (name.equals(gv.getString("group")))
                {
                    return true;
                }
            }
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
        }
        return false;
    }

    public Collection getSubscriptions()
    {
        try
        {
            final List<GenericValue> subList = subscriptionManager.getAllSubscriptions();
            final Collection<String> subscriptions = new ArrayList<String>();
            for (final GenericValue gv : subList)
            {
                if (name.equals(gv.getString("group")))
                {
                    final String userkey = gv.getString("username");
                    final Long filterID = gv.getLong("filterID");

                    final ApplicationUser user = getUserManager().getUserByKey(userkey);
                    final JiraServiceContext ctx = new JiraServiceContextImpl(user);
                    final SearchRequest request = searchRequestService.getFilter(ctx, filterID);

                    if (request != null)
                    {
                        final String filterName = request.getName();
                        final String username = (user == null ? null : user.getUsername());
                        final String text = getText("admin.deletegroup.subscriptions.item", filterName, username);
                        subscriptions.add(text);
                    }
                }
            }
            return subscriptions;
        }
        catch (Exception e)
        {
            log.error(e, e);
            return Collections.EMPTY_LIST;
        }
    }
}
