package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.permission.LiteralSanitiser;
import com.atlassian.jira.jql.permission.ProjectLiteralSanitiser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An abstract class for the versions system field flag functions
 *
 * @since v4.0
 */
public abstract class AbstractVersionsFunction extends AbstractJqlFunction implements ClauseSanitisingJqlFunction
{
    protected final IndexInfoResolver<Project> projectIndexInfoResolver;
    private final NameResolver<Project> projectResolver;
    protected final PermissionManager permissionManager;

    public AbstractVersionsFunction(final NameResolver<Project> projectResolver, final PermissionManager permissionManager)
    {
        this.projectIndexInfoResolver = createIndexInfoResolver(projectResolver);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectResolver = notNull("projectResolver", projectResolver);
    }

    public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        final MessageSet messages = new MessageSetImpl();
        final List<String> args = operand.getArgs();
        final ApplicationUser searcherUser = ApplicationUsers.from(searcher);
        for (String projectArg : args)
        {
            // try to resolve project
            final List<String> projectIds = getFilteredIndexValues(projectArg, false, searcherUser);
            if (projectIds.isEmpty())
            {
                messages.addErrorMessage(getI18n().getText("jira.jql.version.function.project.arg.incorrect", projectArg, getFunctionName()));
            }
        }
        return messages;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        final List<Version> candidateVersions = new ArrayList<Version>();
        final List<String> args = operand.getArgs();
        if (args.size() > 0)
        {
            for (String projectArg : args)
            {
                // try to resolve project
                final List<String> projectIds;
                try
                {
                    projectIds = getFilteredIndexValues(projectArg, queryCreationContext.isSecurityOverriden(), queryCreationContext.getApplicationUser());
                }
                catch (IllegalArgumentException e)
                {
                    // if we got an exception it means more than 1 project was resolved from this argument: just ignore and continue
                    continue;
                }

                if (projectIds.size() != 1)
                {
                    // if we don't have a project id, this should not have passed validation. just skip this guy and keep going.
                    continue;
                }
                String project = projectIds.get(0);
                candidateVersions.addAll(getVersionsForProject(new Long(project)));
            }
        }
        else
        {
            final ApplicationUser user = queryCreationContext.getApplicationUser();
            if (queryCreationContext.isSecurityOverriden())
            {
                candidateVersions.addAll(getAllVersions(ApplicationUsers.toDirectoryUser(user)));
            }
            else
            {
                Collection<Project> projects = permissionManager.getProjects(Permissions.BROWSE, user);
                for (Project project : projects)
                {
                    candidateVersions.addAll(getVersionsForProject(project.getId()));
                }
            }
        }

        final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (Version version : candidateVersions)
        {
            literals.add(new QueryLiteral(operand, version.getId()));
        }

        return literals;
    }

    public FunctionOperand sanitiseOperand(final User searcher, final FunctionOperand operand)
    {
        // if the function has no args, just return it as is
        final List<String> args = operand.getArgs();
        if (args.isEmpty())
        {
            return operand;
        }

        // Note: since arguments are currently only represented by strings, we need to convert to QueryLiterals for
        // sanitising. But since the args are strings, its not possible to get the EMPTY literal as an argument - keep
        // this in mind for later.
        List<QueryLiteral> literals = CollectionUtil.transform(args, new Function<String, QueryLiteral>()
        {
            public QueryLiteral get(final String input)
            {
                return new QueryLiteral(operand, input);
            }
        });

        final ProjectLiteralSanitiser.Result result = createLiteralSanitiser(searcher).sanitiseLiterals(literals);
        if (!result.isModified())
        {
            return operand;
        }
        else
        {
            // Note: as above, we know here that a non-empty literal would never be sanitised into the empty literal
            // so we don't need to worry about the .asString() call below.
            List<String> newArgs = CollectionUtil.transform(result.getLiterals(), new Function<QueryLiteral, String>()
            {
                public String get(final QueryLiteral input)
                {
                    return input.asString();
                }
            });
            return new FunctionOperand(operand.getName(), newArgs);
        }
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.VERSION;
    }

    /**
     * @param projectArg the project argument from the function operand
     * @param overrideSecurity false if we should filter values based on permissions
     * @param searcher the user performing the search
     * @return the index values for the specified project argument, filtered by browse permissions for the resolved project
     * (if available).
     * @throws IllegalArgumentException if the project argument resolved to more than one index value
     */
    private List<String> getFilteredIndexValues(final String projectArg, final boolean overrideSecurity, final ApplicationUser searcher)
    {
        final List<String> projectStringIds = new ArrayList<String>(projectIndexInfoResolver.getIndexedValues(projectArg));
        if (projectStringIds.size() > 1)
        {
            throw new IllegalArgumentException("How did we get more than one project resolving from '" + projectArg + "'?");
        }

        final Iterator<String> disallowedIds = CollectionUtil.filter(projectStringIds.iterator(), new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                Long id = new Long(input);
                if (!overrideSecurity)
                {
                    return !permissionManager.hasPermission(Permissions.BROWSE, projectResolver.get(id), searcher);
                }
                else
                {
                    return false;
                }
            }
        });

        while (disallowedIds.hasNext())
        {
            disallowedIds.next();
            disallowedIds.remove();
        }

        return projectStringIds;
    }

    ///CLOVER:OFF
    protected IndexInfoResolver<Project> createIndexInfoResolver(final NameResolver<Project> projectResolver)
    {
        return new ProjectIndexInfoResolver(notNull("projectResolver", projectResolver));
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    LiteralSanitiser createLiteralSanitiser(final User user)
    {
        return new ProjectLiteralSanitiser(projectResolver, permissionManager, user);
    }
    ///CLOVER:ON

    /**
     * @return all the versions relevant to this function.
     */
    protected abstract Collection<Version> getAllVersions(User user);

    /**
     * @param projectId the id of the {@link com.atlassian.jira.project.Project} which the versions belong to
     * @return all the versions relevant to this function and the specified project
     */
    protected abstract Collection<Version> getVersionsForProject(Long projectId);

}
