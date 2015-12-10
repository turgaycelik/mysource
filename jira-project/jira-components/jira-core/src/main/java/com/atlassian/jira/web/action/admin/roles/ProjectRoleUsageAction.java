package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.scheme.SchemeGVNameComparator;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: detkin Date: Jun 13, 2006 Time: 11:09:38 AM To change this template use File |
 * Settings | File Templates.
 */
@WebSudoRequired
public class ProjectRoleUsageAction extends AbstractProjectRole
{
    private NotificationSchemeManager notificationSchemeManager;
    private PermissionSchemeManager permissionSchemeManager;
    private ProjectFactory projectFactory;
    private WorkflowManager workflowManager;
    private List associatedNotificationSchemes = null;
    private List associatedPermissionSchemes = null;
    private List associatedIssueSecuritySchemes = null;
    private MultiMap associatedWorkflows = null;
    private final Map associatedProjectsByNotificationScheme = new HashMap();
    private final Map associatedProjectsByPermissionScheme = new HashMap();
    private final Map associatedProjectsByIssueSecurityScheme = new HashMap();

    public ProjectRoleUsageAction(ProjectRoleService projectRoleService, NotificationSchemeManager notificationSchemeManager, PermissionSchemeManager permissionSchemeManager, ProjectFactory projectFactory, WorkflowManager workflowManager)
    {
        super(projectRoleService);
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.projectFactory = projectFactory;
        this.workflowManager = workflowManager;
    }

    public List getAssociatedNotificationSchemes()
    {
        if (associatedNotificationSchemes == null)
        {
            associatedNotificationSchemes = new ArrayList(projectRoleService.getAssociatedNotificationSchemes(getRole(), this));
            Collections.sort(associatedNotificationSchemes, SchemeGVNameComparator.getInstance());
        }
        return associatedNotificationSchemes;
    }

    public MultiMap getAssociatedWorkflows()
    {
        if (associatedWorkflows == null)
        {
            associatedWorkflows = projectRoleService.getAssociatedWorkflows(getRole(), this);
        }
        return associatedWorkflows;
    }

    public List getAssociatedPermissionSchemes()
    {
        if (associatedPermissionSchemes == null)
        {
            associatedPermissionSchemes = new ArrayList(projectRoleService.getAssociatedPermissionSchemes(getRole(), this));
            Collections.sort(associatedPermissionSchemes, SchemeGVNameComparator.getInstance());
        }
        return associatedPermissionSchemes;
    }

    public List getAssociatedIssueSecuritySchemes()
    {
        if (associatedIssueSecuritySchemes== null)
        {
            associatedIssueSecuritySchemes = new ArrayList(projectRoleService.getAssociatedIssueSecuritySchemes(getRole(), this));
            Collections.sort(associatedIssueSecuritySchemes, SchemeGVNameComparator.getInstance());
        }
        return associatedIssueSecuritySchemes;
    }

    public Collection getAssociatedProjectsForNotificationScheme(GenericValue scheme) throws GenericEntityException
    {
        if (!associatedProjectsByNotificationScheme.containsKey(scheme))
        {
            associatedProjectsByNotificationScheme.put(scheme, projectFactory.getProjects(notificationSchemeManager.getProjects(scheme)));
        }
        return (Collection) associatedProjectsByNotificationScheme.get(scheme);
    }

    public Collection getAssociatedProjectsForPermissionScheme(GenericValue scheme) throws GenericEntityException
    {
        if (!associatedProjectsByPermissionScheme.containsKey(scheme))
        {
            associatedProjectsByPermissionScheme.put(scheme, projectFactory.getProjects(permissionSchemeManager.getProjects(scheme)));
        }
        return (Collection) associatedProjectsByPermissionScheme.get(scheme);
    }

    public Collection getAssociatedProjectsForIssueSecurityScheme(GenericValue scheme) throws GenericEntityException
    {
        if (!associatedProjectsByIssueSecurityScheme.containsKey(scheme))
        {
            associatedProjectsByIssueSecurityScheme.put(scheme, projectFactory.getProjects(ManagerFactory.getIssueSecuritySchemeManager().getProjects(scheme)));
        }
        return (Collection) associatedProjectsByIssueSecurityScheme.get(scheme);
    }

    public int getMemberCountForProject(Project project)
    {
        ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(getRole(), project, this);
        if (projectRoleActors.getRoleActors() != null)
        {
            return projectRoleActors.getRoleActors().size();
        }
        return 0;
    }

    public String getStepId(long actionDescriptorId, String workflowName)
    {
        return workflowManager.getStepId(actionDescriptorId, workflowName);
    }

}
