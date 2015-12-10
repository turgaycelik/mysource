package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.InjectableComponent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.containsAny;

/**
 * A utlitilty class for generating the clause context specified by
 * a {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}, taking
 * into account what is visible to the searcher.
 *
 * @since v4.0
 */
@InjectableComponent
public class FieldConfigSchemeClauseContextUtil
{
    private static final Logger log = Logger.getLogger(FieldConfigSchemeClauseContextUtil.class);

    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final ConstantsManager constantsManager;
    private final PermissionManager permissionManager;
    private final ProjectFactory projectFactory;

    public FieldConfigSchemeClauseContextUtil(final IssueTypeSchemeManager issueTypeSchemeManager,
            final ConstantsManager constantsManager, final PermissionManager permissionManager,
            final ProjectFactory projectFactory)
    {
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.projectFactory = projectFactory;
    }

    /**
     * Checks if the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} is visible under the given
     * {@link com.atlassian.jira.jql.context.QueryContext}
     *
     * @param queryContext the {@link com.atlassian.jira.jql.context.QueryContext} of the search and user.
     * @param fieldConfigScheme the scheme to to check is visible
     * @return true if the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} is visible under the {@link com.atlassian.jira.jql.context.QueryContext}, false otherwise.
     */
    public boolean isConfigSchemeVisibleUnderContext(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
    {
        if (fieldConfigScheme.isGlobal())
        {
            return true;
        }
        else if (fieldConfigScheme.isAllIssueTypes())
        {
            return fieldConfigSchemeContainsContextProjects(queryContext, fieldConfigScheme);
        }
        else if (fieldConfigScheme.isAllProjects())
        {
            return fieldConfigSchemeContainsContextIssueTypes(queryContext, fieldConfigScheme);
        }
        else
        {
            return fieldConfigSchemeContainsContextMapping(queryContext, fieldConfigScheme);
        }
    }

    /**
     * Finds the most specific {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} associated with a {@link com.atlassian.jira.issue.fields.CustomField}
     * that is viewable from the given {@link com.atlassian.jira.jql.context.QueryContext}. The "Most Specific" means it will always return
     * a FieldConfigScheme with a defined project context if available, otherwise it will return
     * the FieldConfigScheme with the global context (if visible). If no FieldConfigScheme is visible under the QueryContext
     * null is returned.
     *
     * @param queryContext The QueryContext to find the most specific visible {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} for.
     * @param customField The custom field to retreive {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}s from.
     * @return The most specific {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} on teh {@link com.atlassian.jira.issue.fields.CustomField}
     * that is visible to the {@link com.atlassian.jira.jql.context.QueryContext}, null if no {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} is visible.
     */
    public FieldConfigScheme getFieldConfigSchemeFromContext(final QueryContext queryContext, final CustomField customField)
    {
        FieldConfigScheme mostSpecific = null;
        final List<FieldConfigScheme> fieldConfigSchemes = customField.getConfigurationSchemes();
        for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            if (isConfigSchemeVisibleUnderContext(queryContext, fieldConfigScheme))
            {
                if (mostSpecific == null || moreSpecific(mostSpecific, fieldConfigScheme))
                {
                    mostSpecific = fieldConfigScheme;
                }
            }
        }
        return mostSpecific;
    }

    /**
     * Given a {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} determines the
     * {@link com.atlassian.jira.jql.context.ClauseContext} specified by it, taking into account
     * what is visible to the searcher {@link User}
     *
     * @param searcher the user the {@link com.atlassian.jira.jql.context.ClauseContext} is generated for
     * @param fieldConfigScheme the config scheme the {@link com.atlassian.jira.jql.context.ClauseContext} is generated for
     * @return a clause context that is defined by the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} and searchers visibility
     */
    public ClauseContext getContextForConfigScheme(final User searcher, final FieldConfigScheme fieldConfigScheme)
    {
        final Set<ProjectIssueTypeContext> projectIssueTypeContextSet;
        if (fieldConfigScheme.isAllProjects())
        {
            if (fieldConfigScheme.isAllIssueTypes())
            {
                // Generate an All project and issue type context
                projectIssueTypeContextSet = Collections.singleton(ProjectIssueTypeContextImpl.createGlobalContext());
            }
            else
            {
                projectIssueTypeContextSet = addProjectIssueTypeContextsForIssueTypesOnly(fieldConfigScheme);
            }
        }
        else
        {
            final Collection<Project> visibleProjects = permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
            // We need to run through all the visible specified projects and the specified issue types to generate
            // contexts for them.
            List<Project> fieldConfigSchemeProjects = fieldConfigScheme.getAssociatedProjectObjects();
            if (fieldConfigSchemeProjects == null)
            {
                fieldConfigSchemeProjects = Collections.emptyList();
            }
            @SuppressWarnings("unchecked")
            Collection<Project> associatedProjects = CollectionUtils.intersection(visibleProjects, fieldConfigSchemeProjects);
            projectIssueTypeContextSet = addProjectIssueTypeContextsForProjects(fieldConfigScheme, associatedProjects);
        }
        return new ClauseContextImpl(projectIssueTypeContextSet);
    }

    boolean fieldConfigSchemeContainsContextProjects(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
    {
        final Collection<QueryContext.ProjectIssueTypeContexts> contexts = queryContext.getProjectIssueTypeContexts();
        final Set<Long> projectIdsInContext = new HashSet<Long>();

        for (QueryContext.ProjectIssueTypeContexts context : contexts)
        {
            projectIdsInContext.addAll(context.getProjectIdInList());
        }

        final Set<Long> projectIdsInScheme = getProjectIdsForScheme(fieldConfigScheme);

        // simply check that there is an overlap between the projects specified in the field config scheme
        // and those specified by the query context
        return !CollectionUtils.intersection(projectIdsInContext, projectIdsInScheme).isEmpty();
    }

    boolean fieldConfigSchemeContainsContextIssueTypes(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
    {
        final Collection<QueryContext.ProjectIssueTypeContexts> contexts = queryContext.getProjectIssueTypeContexts();
        final Set<String> issueTypeIdsInContext = new HashSet<String>();

        for (QueryContext.ProjectIssueTypeContexts context : contexts)
        {
            issueTypeIdsInContext.addAll(context.getIssueTypeIds());
        }

        final Set<String> issueTypeIdsInScheme = getIssueTypeIdsForScheme(fieldConfigScheme);

        // simply check that there is an overlap between the issue types specified in the field config scheme
        // and those specified by the query context
        return !CollectionUtils.intersection(issueTypeIdsInContext, issueTypeIdsInScheme).isEmpty();
    }

    boolean fieldConfigSchemeContainsContextMapping(final QueryContext queryContext, final FieldConfigScheme fieldConfigScheme)
    {
        final Set<Long> projectIdsForScheme = getProjectIdsForScheme(fieldConfigScheme);
        final Set<String> issueTypeIdsForScheme = getIssueTypeIdsForScheme(fieldConfigScheme);

        // if there exists any context where the project is found in the field config scheme and at least one issue type
        // of that context is also found in the field config scheme, then return true
        final Collection<QueryContext.ProjectIssueTypeContexts> contexts = queryContext.getProjectIssueTypeContexts();
        for (QueryContext.ProjectIssueTypeContexts context : contexts)
        {
            if (!context.getProjectContext().isAll() && projectIdsForScheme.containsAll(context.getProjectIdInList()))
            {
                final List<String> contextIssueTypeIds = context.getIssueTypeIds();
                if (containsAny(issueTypeIdsForScheme, contextIssueTypeIds))
                {
                    return true;
                }
            }
        }

        return false;
    }

    Set<Long> getProjectIdsForScheme(final FieldConfigScheme fieldConfigScheme)
    {
        final List<Project> projects = fieldConfigScheme.getAssociatedProjectObjects();
        final Set<Long> projectIdsInScheme = new HashSet<Long>();
        if (projects != null)
        {
            for (Project project : projects)
            {
                projectIdsInScheme.add(project.getId());
            }
        }
        return projectIdsInScheme;
    }

    Set<String> getIssueTypeIdsForScheme(final FieldConfigScheme fieldConfigScheme)
    {
        final Set<GenericValue> issueTypeGVs = fieldConfigScheme.getAssociatedIssueTypes();
        final Set<String> issueTypeIdsInScheme = new HashSet<String>();
        if (issueTypeGVs == null)
        {
            log.debug("Custom field context with no valid issue types encountered, this context will be ignored, see http://jira.atlassian.com/browse/JRA-10461 for details");
        }
        else
        {
            for (GenericValue issueTypeGV : issueTypeGVs)
            {
                issueTypeIdsInScheme.add(issueTypeGV.getString("id"));
            }
        }
        return issueTypeIdsInScheme;
    }

    Set<ProjectIssueTypeContext> addProjectIssueTypeContextsForProjects(final FieldConfigScheme fieldConfigScheme, final Collection<Project> associatedProjects)
    {
        Set<ProjectIssueTypeContext> relevantContexts = new HashSet<ProjectIssueTypeContext>();
        for (Project associatedProject : associatedProjects)
        {
            final Long projectId = associatedProject.getId();

            // If the config scheme is for all issue types then lets only specify the project
            if (fieldConfigScheme.isAllIssueTypes())
            {
                final ProjectIssueTypeContextImpl projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectId), AllIssueTypesContext.INSTANCE);
                relevantContexts.add(projectIssueTypeContext);
            }
            else if (fieldConfigScheme.getAssociatedIssueTypes() == null)
            {
                log.debug("Custom field context with no valid issue types encountered, this context will be ignored, see http://jira.atlassian.com/browse/JRA-10461 for details");
            }
            // Otherwise we need to run through all the issue types specified in the config scheme and add a project/issue type context for that.
            else
            {
                final Collection<IssueType> projectIssueTypes = issueTypeSchemeManager.getIssueTypesForProject(associatedProject);
                final Set<GenericValue> associatedIssueTypeGvs = fieldConfigScheme.getAssociatedIssueTypes();

                // Only run through the config schemes associated issue types
                for (GenericValue associatedIssueTypeGv : associatedIssueTypeGvs)
                {
                    final String issueTypeId = associatedIssueTypeGv.getString("id");
                    final IssueType associatedIssueType = constantsManager.getIssueTypeObject(issueTypeId);

                    // We need to make sure that the issue type that was specified is relevant for this project. The
                    // custom field configuration screen does not check to see that project/issue type combo's make sense.
                    if (projectIssueTypes.contains(associatedIssueType))
                    {
                        final ProjectIssueTypeContextImpl projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectId), new IssueTypeContextImpl(issueTypeId));
                        relevantContexts.add(projectIssueTypeContext);
                    }
                }
            }
        }
        return relevantContexts;
    }

    /**
     * @param fieldConfigScheme the field configuration scheme
     * @return the set of ProjectIssueTypeContexts for the combinations of [All Projects, Implicit Issue Type] for each
     * issue type associated to the config scheme.
     */
    Set<ProjectIssueTypeContext> addProjectIssueTypeContextsForIssueTypesOnly(final FieldConfigScheme fieldConfigScheme)
    {
        final Set<GenericValue> associatedIssueTypeGvs = fieldConfigScheme.getAssociatedIssueTypes();
        final Set<ProjectIssueTypeContext> contexts = new LinkedHashSet<ProjectIssueTypeContext>();
        if (associatedIssueTypeGvs == null)
        {
            log.debug("Custom field context with no valid issue types encountered, this context will be ignored, see http://jira.atlassian.com/browse/JRA-10461 for details");
        }
        else
        {
            // Only run through the config schemes associated issue types
            for (GenericValue associatedIssueTypeGv : associatedIssueTypeGvs)
            {
                final String issueTypeId = associatedIssueTypeGv.getString("id");
                contexts.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl(issueTypeId)));
            }
        }
        return contexts;
    }

    /*
     * true if scheme2 is more specific than scheme1 or equal to
     */
    private boolean moreSpecific(FieldConfigScheme scheme1, FieldConfigScheme scheme2)
    {
        if (scheme1.isGlobal())
        {
            return true;
        }
        else if (scheme1.isAllProjects())
        {
            return !scheme2.isAllIssueTypes() || !scheme2.isAllProjects();
        }
        else if (scheme1.isAllIssueTypes())
        {
            return !scheme2.isAllProjects();
        }
        else
        {
            return !scheme2.isAllIssueTypes() && !scheme2.isAllProjects();
        }
    }
}
