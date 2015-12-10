package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleComparator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.json.JSONArray;
import org.apache.commons.lang.StringEscapeUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.util.TextUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionUtil.sort;

/**
 * A {@link com.atlassian.jira.sharing.type.ShareTypeRenderer} for the {@link com.atlassian.jira.sharing.type.ProjectShareType}.
 *
 * @since v3.13
 */
public class ProjectShareTypeRenderer extends VelocityShareTypeRenderer
{
    private static final String PROJECTS_KEY = "projects";
    private static final String ROLES_KEY = "roles";
    private static final String ROLES_MAP = "rolesMap";
    private static final String BOLD_START = "<b>";
    private static final String BOLD_END = "</b>";
    private static final String BR_TAG = "<br/>";

    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;
    private final PermissionManager permissionManager;
    private final ProjectFactory projectFactory;

    public ProjectShareTypeRenderer(final EncodingConfiguration encoding, final VelocityTemplatingEngine templatingEngine,
            final ProjectManager projectManager, final ProjectRoleManager projectRoleManager,
            final PermissionManager permissionManager, final ProjectFactory projectFactory)
    {
        super(encoding, templatingEngine);

        Assertions.notNull("projectManager", projectManager);
        Assertions.notNull("projectRoleManager", projectRoleManager);
        Assertions.notNull("permissionManager", permissionManager);
        Assertions.notNull("projectFactory", projectFactory);

        this.projectFactory = projectFactory;
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
    }

    public String renderPermission(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", ProjectShareType.TYPE, permission.getType());

        final String projectName = getProject(permission.getParam1(), authenticationContext.getI18nHelper());

        if (permission.getParam2() == null)
        {
            return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.project", ProjectShareTypeRenderer.BOLD_START,
                ProjectShareTypeRenderer.BOLD_END, projectName);
        }
        final String roleName = getRole(permission.getParam2(), authenticationContext.getI18nHelper());
        return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.project.role", ProjectShareTypeRenderer.BOLD_START,
            ProjectShareTypeRenderer.BOLD_END, projectName, ProjectShareTypeRenderer.BR_TAG + ProjectShareTypeRenderer.BOLD_START,
            ProjectShareTypeRenderer.BOLD_END, roleName);
    }

    public String getSimpleDescription(final SharePermission permission, final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.equals("permission-type", ProjectShareType.TYPE, permission.getType());

        final String projectName = getProject(permission.getParam1(), authenticationContext.getI18nHelper());

        if (permission.getParam2() == null)
        {
            return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.project.desc", projectName);
        }
        final String roleName = getRole(permission.getParam2(), authenticationContext.getI18nHelper());
        return authenticationContext.getI18nHelper().getText("common.sharing.shared.display.project.role.desc", roleName, projectName);
    }

    public Map<String, String> getTranslatedTemplates(final JiraAuthenticationContext authenticationContext, final TypeDescriptor<? extends SharedEntity> type, final RenderMode mode)
    {
        Assertions.notNull("authenticationContext", authenticationContext);
        Assertions.notNull("type", type);
        Assertions.notNull("mode", mode);

        final Map<String, String> templates = new HashMap<String, String>();

        templates.put("share_invalid_project", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
            "common.sharing.shared.project.not.found")));
        templates.put("share_invalid_role", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
            "common.sharing.shared.role.not.found")));

        if (mode == RenderMode.EDIT)
        {
            templates.put("share_project_display_all", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.project", ProjectShareTypeRenderer.BOLD_START, ProjectShareTypeRenderer.BOLD_END)));
            templates.put("share_project_display", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.project.role", ProjectShareTypeRenderer.BOLD_START, ProjectShareTypeRenderer.BOLD_END,
                ProjectShareTypeRenderer.BOLD_START, ProjectShareTypeRenderer.BOLD_END)));

            templates.put("share_project_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.project.desc")));
            templates.put("share_role_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.shared.template.project.role.desc")));

        }
        else if (mode == RenderMode.SEARCH)
        {
            /**
             * This code generated the translation key based on the ShareType. This means the following keys are used:
             * common.sharing.search.template.project.desc.SearchRequest
             * common.sharing.search.template.project.desc.PortalPage
             * common.sharing.search.template.project.role.desc.SearchRequest
             * common.sharing.search.template.project.role.desc.PortalPage
             */

            templates.put("share_project_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.search.template.project.desc." + type.getName())));
            templates.put("share_role_description", StringEscapeUtils.escapeJavaScript(authenticationContext.getI18nHelper().getText(
                "common.sharing.search.template.project.role.desc." + type.getName())));
        }

        return Collections.unmodifiableMap(templates);
    }

    public String getShareTypeEditor(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);

        final Map<String, Object> params = new HashMap<String, Object>();
        final Collection<Project> projects = projectFactory.getProjects(getProjects(authenticationContext.getLoggedInUser()));

        final Set<ProjectRole> roles = new HashSet<ProjectRole>();
        final Map<Long, String> rolesMap = new HashMap<Long, String>();
        for (final Project project : projects)
        {
            final JSONArray array = new JSONArray();
            Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles(authenticationContext.getLoggedInUser(), project);
            roles.addAll(projectRoles);
            projectRoles = sort(projectRoles, ProjectRoleComparator.COMPARATOR);
            for (final Object element : projectRoles)
            {
                final ProjectRole role = (ProjectRole) element;
                array.put(role.getId());
            }
            rolesMap.put(project.getId(), array.toString());
        }

        params.put(ProjectShareTypeRenderer.PROJECTS_KEY, sort(projects, ProjectNameComparator.COMPARATOR));
        params.put(ProjectShareTypeRenderer.ROLES_KEY, sort(roles, ProjectRoleComparator.COMPARATOR));
        params.put(ProjectShareTypeRenderer.ROLES_MAP, rolesMap);

        return renderVelocity("share-type-project-selector.vm", params, authenticationContext);
    }

    public boolean isAddButtonNeeded(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);
        return !getProjects(authenticationContext.getLoggedInUser()).isEmpty();
    }

    public String getShareTypeLabel(final JiraAuthenticationContext authenticationContext)
    {
        Assertions.notNull("authenticationContext", authenticationContext);
        return authenticationContext.getI18nHelper().getText("common.words.project");
    }

    private String getRole(final String roleId, final I18nHelper i18nHelper)
    {
        final ProjectRole role = projectRoleManager.getProjectRole(new Long(roleId));
        if (role == null)
        {
            return i18nHelper.getText("common.sharing.shared.role.not.found");
        }
        return TextUtil.escapeHTML(role.getName());
    }

    private String getProject(final String projectId, final I18nHelper i18nHelper)
    {
        final Project project = projectManager.getProjectObj(new Long(projectId));
        if (project == null)
        {
            return i18nHelper.getText("common.sharing.shared.project.not.found");
        }
        return TextUtil.escapeHTML(project.getName());
    }

    private Collection<GenericValue> getProjects(final User user)
    {
        final Collection<GenericValue> projects = permissionManager.getProjects(Permissions.BROWSE, user);
        if (projects == null)
        {
            return Collections.emptyList();
        }
        return projects;
    }
}
