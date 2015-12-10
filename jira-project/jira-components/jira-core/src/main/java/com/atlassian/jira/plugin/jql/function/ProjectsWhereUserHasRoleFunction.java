package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This function returns a list of projects where the user has the requested permission.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * component lead.
 *
 * @since v4.2
 */
public class ProjectsWhereUserHasRoleFunction extends AbstractUserCapabilityFunction
{
    public static final String FUNCTION_PROJECTS_WHERE_USER_HAS_PERMISSION = "projectsWhereUserHasRole";
    private static final String JIRA_JQL_PROJECT_NO_SUCH_USER = "jira.jql.project.no.such.user";

    private final PermissionManager permissionManager;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectManager projectManager;

    public ProjectsWhereUserHasRoleFunction(final PermissionManager permissionManager, final ProjectRoleManager projectRoleManager, final ProjectManager projectManager, final UserUtil userUtil)
    {
        super(userUtil);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectRoleManager = notNull("roleManager", projectRoleManager);
        this.projectManager = notNull("projectManager", projectManager);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.PROJECT;
    }

    protected MessageSet validateCapability(final String roleName, final I18nHelper i18n)
    {
        MessageSet messageSet = new MessageSetImpl();
        // Check the role requested exists
        if (projectRoleManager.getProjectRole(roleName) == null)
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.project.no.such.role", getFunctionName(), roleName));
        }
        return messageSet;
    }


    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final ApplicationUser user, final String roleName)
    {
        ProjectRole projectRole = projectRoleManager.getProjectRole(roleName);
        if (projectRole == null)
        {
            return Collections.emptyList();
        }
        List<QueryLiteral> values = new ArrayList<QueryLiteral>();

        final List<Project> allProjects = projectManager.getProjectObjects();
        for (Project project : allProjects)
        {
            if (projectRoleManager.isUserInProjectRole(user, projectRole, project))
            {
                if (queryCreationContext.isSecurityOverriden() || permissionManager.hasPermission(Permissions.BROWSE, project, queryCreationContext.getApplicationUser()))
                {
                    values.add(new QueryLiteral(functionOperand, project.getId()));
                }
            }
        }
        return values;
    }

    protected String getUserNotFoundMessageKey()
    {
        return JIRA_JQL_PROJECT_NO_SUCH_USER;
    }}
