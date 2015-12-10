package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.GenericValueFunctions;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.Query;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
@InjectableComponent
public class FieldLayoutSchemeHelperImpl implements FieldLayoutSchemeHelper
{
    private static final Logger log = Logger.getLogger(FieldLayoutSchemeHelperImpl.class);

    private final FieldLayoutManager fieldLayoutManager;
    private final SearchProvider searchProvider;
    private static final Function<GenericValue,Long> GV_TO_ID_TRANSFORMER = GenericValueFunctions.getStringAsLong("id");

    public FieldLayoutSchemeHelperImpl(final FieldLayoutManager fieldLayoutManager, final SearchProvider searchProvider)
    {
        this.fieldLayoutManager = notNull("fieldLayoutManager", fieldLayoutManager);
        this.searchProvider = notNull("searchProvider", searchProvider);
    }

    public boolean doesChangingFieldLayoutAssociationRequireMessage(final User user, final FieldLayoutScheme fieldLayoutScheme, final Long oldFieldLayoutId, final Long newFieldLayoutId)
    {
        boolean messageRequired = false;
        if (!fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(oldFieldLayoutId, newFieldLayoutId))
        {
            messageRequired = doProjectsHaveIssues(user, fieldLayoutScheme.getProjects());
        }

        return messageRequired;
    }

    public boolean doesChangingFieldLayoutRequireMessage(final User user, final EditableFieldLayout fieldLayout)
    {
        return doProjectsHaveIssues(user, fieldLayoutManager.getRelatedProjects(fieldLayout));
    }

    public boolean doesChangingFieldLayoutSchemeForProjectRequireMessage(final User user, final Long projectId, final Long oldFieldLayoutSchemeId, final Long newFieldLayoutSchemeId)
    {
        boolean messageRequired = false;
        if (!fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(oldFieldLayoutSchemeId, newFieldLayoutSchemeId))
        {
            messageRequired = doProjectsHaveIssues(user, Collections.singletonList(projectId));
        }
        return messageRequired;
    }

    /**
     * @param user the user
     * @param projects the projects to check; if empty the result will be false.
     * @return if there are any issues in the scheme's associated projects
     */
    boolean doProjectsHaveIssues(final User user, final Collection<GenericValue> projects)
    {
        final List<Long> projectIds = CollectionUtil.transform(projects.iterator(), GV_TO_ID_TRANSFORMER);
        return doProjectsHaveIssues(user, projectIds);
    }

    private boolean doProjectsHaveIssues(final User user, final List<Long> projectIds)
    {
        if (projectIds.isEmpty())
        {
            return false;
        }
        final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().project().inNumbers(projectIds);
        return doesQueryHaveIssues(user, builder.buildQuery());
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
