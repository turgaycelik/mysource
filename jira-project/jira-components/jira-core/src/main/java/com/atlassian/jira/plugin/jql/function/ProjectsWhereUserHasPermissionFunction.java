package com.atlassian.jira.plugin.jql.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.FunctionOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Locale.ENGLISH;

/**
 * This function returns a list of projects where the user has the requested permission.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * component lead.
 *
 * @since v4.2
 */
public class ProjectsWhereUserHasPermissionFunction extends AbstractUserCapabilityFunction
{
    public static final String FUNCTION_PROJECTS_WHERE_USER_HAS_PERMISSION = "projectsWhereUserHasPermission";
    private static final String JIRA_JQL_PROJECT_NO_SUCH_USER = "jira.jql.project.no.such.user";

    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nHelperFactory;

    public ProjectsWhereUserHasPermissionFunction(final PermissionManager permissionManager,
            final UserUtil userUtil, final I18nHelper.BeanFactory i18nHelperFactory)
    {
        super(userUtil);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.i18nHelperFactory = notNull("i18nHelperFactory", i18nHelperFactory);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.PROJECT;
    }

    protected MessageSet validateCapability(final String permissionName, final I18nHelper i18n)
    {
        MessageSet messageSet = new MessageSetImpl();
        // Check the permission requested exists
        if (getPermissionByName(permissionName) == null)
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.project.no.such.permission", getFunctionName(), permissionName));
        }
        return messageSet;
    }

    private ProjectPermission getPermissionByName(String name)
    {
        I18nHelper i18nHelper = i18nHelperFactory.getInstance(ENGLISH);
        for (ProjectPermission permission : permissionManager.getAllProjectPermissions())
        {
            String permissionName = i18nHelper.getText(permission.getNameI18nKey());
            if (permissionName.equalsIgnoreCase(name))
            {
                return permission;
            }
        }
        return null;
    }

    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final ApplicationUser user, final String permissionName)
    {
        ProjectPermission permission = getPermissionByName(permissionName);
        if (permission == null)
        {
            return Collections.emptyList();
        }

        List<QueryLiteral> values = new ArrayList<QueryLiteral>();

        Collection<Project> projects = permissionManager.getProjects(new ProjectPermissionKey(permission.getKey()), user);
        for (Project project : projects)
        {
            if (!queryCreationContext.isSecurityOverriden())
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
    }
}
