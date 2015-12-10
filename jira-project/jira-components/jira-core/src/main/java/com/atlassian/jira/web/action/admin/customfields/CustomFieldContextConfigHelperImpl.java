package com.atlassian.jira.web.action.admin.customfields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.Query;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
@InjectableComponent
public class CustomFieldContextConfigHelperImpl implements CustomFieldContextConfigHelper
{
    private static final Logger log = Logger.getLogger(CustomFieldContextConfigHelperImpl.class);

    private final SearchProvider searchProvider;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;

    public CustomFieldContextConfigHelperImpl(final SearchProvider searchProvider, final FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.fieldConfigSchemeManager = notNull("fieldConfigSchemeManager", fieldConfigSchemeManager);
    }

    public boolean doesAddingContextToCustomFieldAffectIssues(final User user, final CustomField customField, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes, final boolean isNewCustomField)
    {
        if (!isNewCustomField && doesCustomFieldHaveGlobalScheme(customField))
        {
            return false;
        }
        else
        {
            return doesContextHaveIssues(user, projectContexts, issueTypes);
        }
    }

    public boolean doesChangingContextAffectIssues(final User user, final CustomField customField, final FieldConfigScheme oldFieldConfigScheme, final boolean isNewSchemeGlobal, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
    {
        boolean isOldSchemeGlobal = oldFieldConfigScheme.isGlobal();

        // if we are not changing or changing to a global context
        if (!isOldSchemeGlobal && !isNewSchemeGlobal)
        {
            // if there is another scheme that is global, then no issues affected
            if (doesCustomFieldHaveGlobalScheme(customField))
            {
                return false;
            }
            else
            {
                // otherwise, check for issues in the pre edit and post edit schemes
                if (doesContextHaveIssues(user, projectContexts, issueTypes))
                {
                    return true;
                }
                else
                {
                    return doesFieldConfigSchemeHaveIssues(user, oldFieldConfigScheme);
                }
            }
        }
        else
        {
            // check for issues in the global context
            return doesGlobalContextHaveIssues(user);
        }
    }

    public boolean doesRemovingSchemeFromCustomFieldAffectIssues(final User user, final CustomField customField, final FieldConfigScheme fieldConfigScheme)
    {
        // if removing a global scheme
        if (fieldConfigScheme.isGlobal())
        {
            // Check if there exists any issues which do not fall under the scope of other context configurations for the specified custom field.
            // For example, if a custom field had a context defined for [Global], [project MKY] and [project HSP, issue type Bug],
            // we need to check for any issues which are not in MKY, and are not Bugs in HSP.
            final List<FieldConfigScheme> nonGlobalSchemes = getNonGlobalSchemesForCustomField(customField);
            if (nonGlobalSchemes.isEmpty())
            {
                // no other contexts - just check for issues in global context
                return doesGlobalContextHaveIssues(user);
            }
            else
            {
                // build up query to find issues not in other contexts
                final JqlClauseBuilder clauseBuilder = JqlQueryBuilder.newClauseBuilder().defaultAnd();

                for (FieldConfigScheme scheme : nonGlobalSchemes)
                {
                    List<GenericValue> projects = scheme.getAssociatedProjects();
                    projects = projects == null ? Collections.<GenericValue>emptyList() : Collections.unmodifiableList(projects);
                    Set<GenericValue> issueTypes = scheme.getAssociatedIssueTypes();
                    issueTypes = issueTypes == null ? Collections.<GenericValue>emptySet() : Collections.unmodifiableSet(issueTypes);

                    List<Long> projectIds = filterAndTransformGenericValues(projects);
                    List<Long> issueTypeIds = filterAndTransformGenericValues(issueTypes);

                    clauseBuilder.not().addClause(buildClause(projectIds, issueTypeIds).buildClause());
                }

                return doesQueryHaveIssues(user, clauseBuilder.buildQuery());
            }
        }
        else
        {
            // since we are removing a non-global context, if we still have a global context then no issues are affected
            if (doesCustomFieldHaveGlobalScheme(customField))
            {
                return false;
            }
            else
            {
                // otherwise find out if any issues are in the context of the scheme we are removing
                return doesFieldConfigSchemeHaveIssues(user, fieldConfigScheme);
            }
        }
    }

    /**
     * Determines if there are any issues present under the specified project and issue type context.
     *
     * @param user the current user
     * @param projectContexts project contexts; must not be null
     * @param issueTypes issue type GVs; may be null
     * @return if there was at least one issue present under the context
     */
    boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
    {
        final List<Long> projectIds = new ArrayList<Long>();
        for (JiraContextNode context : projectContexts)
        {
            if (context != null)
            {
                if (context.getProjectObject() != null)
                {
                    projectIds.add(context.getProjectObject().getId());
                }
            }
        }

        List<Long> issueTypeIds = new ArrayList<Long>();
        if (issueTypes != null)
        {
            issueTypeIds = filterAndTransformGenericValues(issueTypes);
        }

        return _doesContextHaveIssues(user, projectIds, issueTypeIds);
    }

    /**
     * Determines if there are any issues present under the specified project and issue type context.
     *
     * @param user the current user
     * @param projects projects in context contexts; must not be null
     * @param issueTypes issue type GVs in context; may be null
     * @return if there was at least one issue present under the context
     */
    boolean doesContextHaveIssues(final User user, final List<GenericValue> projects, final Set<GenericValue> issueTypes)
    {
        List<Long> projectIds = new ArrayList<Long>();
        if (projects != null)
        {
            projectIds = filterAndTransformGenericValues(projects);
        }

        List<Long> issueTypeIds = new ArrayList<Long>();
        if (issueTypes != null)
        {
            issueTypeIds = filterAndTransformGenericValues(issueTypes);
        }

        return _doesContextHaveIssues(user, projectIds, issueTypeIds);
    }

    /**
     * Determines if there are any issues present under the global context.
     *
     * @param user the current user
     * @return if there was at least one issue present under the global context
     */
    boolean doesGlobalContextHaveIssues(final User user)
    {
        return _doesContextHaveIssues(user, Collections.<Long>emptyList(), Collections.<Long>emptyList());
    }

    boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
    {
        final JqlClauseBuilder clauseBuilder = buildClause(projectIds, issueTypeIds);
        return doesQueryHaveIssues(user, clauseBuilder.buildQuery());
    }

    /**
     * Determines if there are any issues present under the context of the field config scheme.
     *
     * @param user the current user
     * @param fieldConfigScheme the field config scheme
     * @return if there was at least one issue present under the context
     */
    boolean doesFieldConfigSchemeHaveIssues(final User user, final FieldConfigScheme fieldConfigScheme)
    {
        List<GenericValue> projects = fieldConfigScheme.getAssociatedProjects();
        projects = projects == null ? Collections.<GenericValue>emptyList() : Collections.unmodifiableList(projects);
        Set<GenericValue> issueTypes = fieldConfigScheme.getAssociatedIssueTypes();
        issueTypes = issueTypes == null ? Collections.<GenericValue>emptySet() : Collections.unmodifiableSet(issueTypes);
        return doesContextHaveIssues(user, projects, issueTypes);
    }

    private boolean doesCustomFieldHaveGlobalScheme(final CustomField customField)
    {
        final List<FieldConfigScheme> schemes = fieldConfigSchemeManager.getConfigSchemesForField(customField);
        for (FieldConfigScheme scheme : schemes)
        {
            if (scheme.isGlobal())
            {
                return true;
            }
        }
        return false;
    }

    private List<FieldConfigScheme> getNonGlobalSchemesForCustomField(final CustomField customField)
    {
        final List<FieldConfigScheme> schemes = fieldConfigSchemeManager.getConfigSchemesForField(customField);
        final List<FieldConfigScheme> result = new ArrayList<FieldConfigScheme>();
        for (FieldConfigScheme scheme : schemes)
        {
            if (!scheme.isGlobal())
            {
                result.add(scheme);
            }
        }
        return result;
    }

    private JqlClauseBuilder buildClause(final List<Long> projectIds, final List<Long> issueTypeIds)
    {
        final JqlClauseBuilder clauseBuilder = JqlQueryBuilder.newClauseBuilder().defaultAnd();
        if (!projectIds.isEmpty())
        {
            clauseBuilder.project().inNumbers(projectIds);
        }
        if (!issueTypeIds.isEmpty())
        {
            clauseBuilder.issueType().inNumbers(issueTypeIds);
        }
        return clauseBuilder;
    }

    /**
     * @param issueTypes issue types represented by generic values; may contain nulls, but must not be null
     * @return a list of the ids of the generic values; the nulls in the input list are ignored.
     */
    private List<Long> filterAndTransformGenericValues(final Collection<GenericValue> issueTypes)
    {
        final Predicate<GenericValue> notNullPredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return input != null;
            }
        };
        final Function<GenericValue, Long> idTransformer = GenericValueFunctions.getStringAsLong("id");

        return CollectionUtil.transform(CollectionUtil.filter(issueTypes.iterator(), notNullPredicate), idTransformer);
    }

    private boolean doesQueryHaveIssues(final User user, final Query query)
    {
        try
        {
            final long issueCount = searchProvider.searchCountOverrideSecurity(query, user);
            return (issueCount > 0);
        }
        catch (SearchException e)
        {
            log.warn(e, e);
            // can't determine whether or not there are issues - but let's just pretend there are
            return true;
        }
    }
}
