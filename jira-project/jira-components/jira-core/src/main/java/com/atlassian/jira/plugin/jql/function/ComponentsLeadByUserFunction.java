package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Collections2.filter;

/**
 * This function returns a list of components lead by a user.
 * <p/>
 * This function expects zero or one argument. If zero arguments are supplied the current logged in user will be used as
 * component lead.
 *
 * @since v4.2
 */
public class ComponentsLeadByUserFunction extends AbstractUserBasedFunction
{
    public static final String FUNCTION_COMPONENTS_LEAD_BY_USER = "componentsLeadByUser";
    private static final String JIRA_JQL_COMPONENT_NO_SUCH_USER = "jira.jql.component.no.such.user";

    private final PermissionManager permissionManager;
    private final ProjectComponentManager componentManager;

    public ComponentsLeadByUserFunction(final PermissionManager permissionManager, final ProjectComponentManager componentManager, final UserUtil userUtil)
    {
        super(userUtil);
        this.permissionManager = permissionManager;
        this.componentManager = notNull("componentManager", componentManager);
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.COMPONENT;
    }

    protected List<QueryLiteral> getFunctionValuesList(final QueryCreationContext queryCreationContext, final FunctionOperand functionOperand, final ApplicationUser user)
    {
        final List<ProjectComponent> candidateComponents = new ArrayList<ProjectComponent>();
        if (queryCreationContext.isSecurityOverriden())
        {
            final Collection<ProjectComponent> components = getLeadComponents(user);
            candidateComponents.addAll(components);
        }
        Collection<Project> projects = permissionManager.getProjects(Permissions.BROWSE, queryCreationContext.getApplicationUser());
        for (Project project : projects)
        {
            candidateComponents.addAll(filter(project.getProjectComponents(), new Predicate<ProjectComponent>()
            {
                @Override
                public boolean apply(@Nullable final ProjectComponent input)
                {
                    return user.getKey().equals(input.getLead());
                }
            }));
        }
        List<QueryLiteral> values = new ArrayList<QueryLiteral>();
        for (ProjectComponent component : candidateComponents)
        {
            values.add(new QueryLiteral(functionOperand, component.getId()));
        }
        return values;
    }

    private Collection<ProjectComponent> getLeadComponents(ApplicationUser user)
    {
        return componentManager.findComponentsByLead(user.getName());
    }

    protected String getUserNotFoundMessageKey()
    {
        return JIRA_JQL_COMPONENT_NO_SUCH_USER;
    }
}
