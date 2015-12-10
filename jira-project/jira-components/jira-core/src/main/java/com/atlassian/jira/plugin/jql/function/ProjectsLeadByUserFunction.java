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
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This function returns a list of projects lead by a user.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * project lead.
 *
 * @since v4.2
 */
public class ProjectsLeadByUserFunction extends AbstractUserBasedFunction
{
    public static final String FUNCTION_PROJECTS_LEAD_BY_USER = "projectsLeadByUser";
    private static final String JIRA_JQL_PROJECT_NO_SUCH_USER = "jira.jql.project.no.such.user";

    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;

    public ProjectsLeadByUserFunction(final PermissionManager permissionManager, final ProjectManager projectManager, final UserUtil userUtil)
    {
        super(userUtil);
        this.permissionManager = permissionManager;
        this.projectManager = notNull("projectManager", projectManager);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.PROJECT;
    }

    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final ApplicationUser user)
    {
        final Iterable<Project> projects = projectManager.getProjectsLeadBy(user);

        List<QueryLiteral> values = new ArrayList<QueryLiteral>();
        for (Project project : projects)
        {
            if (queryCreationContext.isSecurityOverriden() || permissionManager.hasPermission(Permissions.BROWSE, project, queryCreationContext.getApplicationUser()))
            {
                values.add(new QueryLiteral(functionOperand, project.getId()));
            }
        }
        return values;
    }

    protected String getUserNotFoundMessageKey()
    {
        return JIRA_JQL_PROJECT_NO_SUCH_USER;
    }
}
