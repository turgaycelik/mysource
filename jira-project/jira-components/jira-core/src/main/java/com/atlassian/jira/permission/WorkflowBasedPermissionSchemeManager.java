package com.atlassian.jira.permission;

import com.atlassian.cache.CacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getId;
import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getKey;

/**
 * Custom permission scheme manager that allows the list of assignable users to be restricted by workflow state.
 * In jira-workflow.xml, every step can have a meta attribute stating which groups are assignable:
 * <p/>
 * &lt;step id="23" name="Pending Biz User Approval">
 * ....
 * &lt;meta name="jira.permission.assignable.group">${pkey}-bizusers&lt;/meta>
 * <p/>
 * When {@link #getUsers(Long, PermissionContext)} is called to discover assignable users,
 * this permission scheme manager first does the regular "Assignable" check, and then filters returned users for membership
 * of the 'jira.permission.assignable.group' (in this example).  If jira.permission.assignable.* isn't specified for a step, all permission-derived users
 * are returned.
 * @see com.atlassian.jira.security.WorkflowBasedPermissionManager
 */
public class WorkflowBasedPermissionSchemeManager extends DefaultPermissionSchemeManager implements Startable
{
    private static final Logger log = Logger.getLogger(WorkflowBasedPermissionSchemeManager.class);
    private SubTaskManager subTaskManager;
    private WorkflowPermissionFactory workflowPermissionFactory;
    private PermissionContextFactory permissionContextFactory;
    private final EventPublisher eventPublisher;

    public WorkflowBasedPermissionSchemeManager(ProjectManager projectManager, PermissionTypeManager permissionTypeManager,
            WorkflowPermissionFactory workflowPermissionFactory, PermissionContextFactory permissionContextFactory,
            OfBizDelegator ofBizDelegator, SchemeFactory schemeFactory, final EventPublisher eventPublisher, final NodeAssociationStore nodeAssociationStore, final GroupManager groupManager, final CacheManager cacheManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, ofBizDelegator, schemeFactory, nodeAssociationStore, groupManager, eventPublisher, cacheManager);
        this.workflowPermissionFactory = workflowPermissionFactory;
        this.permissionContextFactory = permissionContextFactory;
        this.eventPublisher = eventPublisher;
        subTaskManager = ComponentAccessor.getSubTaskManager();
    }

    @Override
    public void start() throws Exception
    {
        super.start();
        eventPublisher.register(this);
    }

    @com.atlassian.event.api.EventListener
    @Override
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
    }

    @Override
    public Collection<User> getUsers(Long permissionId, final PermissionContext ctx)
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        return getUsers(permissionKey, ctx);
    }

    @Override
    public Collection<User> getUsers(ProjectPermissionKey permissionKey, final PermissionContext ctx)
    {
        Collection<User> users = super.getUsers(permissionKey, ctx);

        // TODO: work with permission keys directly (see SOFT-85).
        Integer permissionId = getId(permissionKey);
        if (permissionId == null)
        {
            return users;
        }

        // Get workflow permission overrides from this issue's workflow step and its parent's
        if (ctx.hasIssuePermissions())
        {
            List workflowPerms = workflowPermissionFactory.getWorkflowPermissions(ctx, permissionId.intValue(), false);
            if (subTaskManager.isSubTasksEnabled() && ctx.getIssue().getParentObject() != null)
            {
                PermissionContext parentCtx = permissionContextFactory.getPermissionContext(ctx.getIssue().getParentObject());
                workflowPerms.addAll(workflowPermissionFactory.getWorkflowPermissions(parentCtx, permissionId.intValue(), true));
            }

            if (workflowPerms.size() > 0)
            {
                // We have 1 or more workflow permission overrides; evaluate them to get the list of allowed users
                Set allowedUsers = new HashSet(users.size());
                Iterator iter = workflowPerms.iterator();
                while (iter.hasNext())
                {
                    WorkflowPermission perm = (WorkflowPermission) iter.next();
                    try
                    {
                        Collection newUsers = perm.getUsers(ctx);
                        log.info(perm+" added users: "+listUsers(newUsers));
                        allowedUsers.addAll(newUsers);
                    }
                    catch (IllegalArgumentException e)
                    {
                        log.error("Error with workflow permission" + perm +": "+e.getMessage(), e);
                        throw new RuntimeException("Error with workflow permission "+perm+": "+e.getMessage());
                    }
                }
                log.info("Retaining " + listUsers(allowedUsers) + " of " + listUsers(users));
                // Return the intersection of permission scheme users and workflow permission users
                users.retainAll(allowedUsers);
            }
        }

        return users;
    }

    /** Print a user list for debugging. */
    private String listUsers(final Collection newUsers)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (Iterator iterator = newUsers.iterator(); iterator.hasNext();)
        {
            User user = (User) iterator.next();
            buf.append(user.getName());
            if (iterator.hasNext()) buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }
}
