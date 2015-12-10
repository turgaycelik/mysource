package com.atlassian.jira.issue.fields;

import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.ProjectIssueTypeContext;
import com.atlassian.jira.jql.context.ProjectIssueTypeContextImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A utility that responsible for providing a SearchContext from a list of visible projects
 * and a custom field whose configuration limits it to certain project/issuetype pairs.
 *
 * @since v4.0
 */
public class FieldContextGenerator
{
    private FieldVisibilityManager fieldVisibilityManager;

    public FieldContextGenerator(FieldVisibilityManager fieldVisibilityManager)
    {
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
    }

    /**
     * Creates the {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext Contexts} that
     * represent the combinations of project and issue type for which the given field is visible.
     *
     * @param projects some projects to get SchemeContexts for.
     * @param fieldId  the field to use for visibility checking.
     * @return the set of SchemeContexts.
     */
    public ClauseContext generateClauseContext(List<Project> projects, String fieldId)
    {
        return new ClauseContextImpl(generateSearchContext(projects, fieldId));
    }

    private Set<ProjectIssueTypeContext> generateSearchContext(final List<Project> projects, final String fieldId)
    {
        boolean visibleSomewhere = false;
        for (Project project : projects)
        {
            if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(project.getId(), fieldId))
            {
                visibleSomewhere = true;
            }
        }
        // If the field is not visible in any scheme the user can see then there is no context, otherwise it will be
        // all visible projects
        if (!visibleSomewhere)
        {
            return Collections.emptySet();
        }
        return Collections.singleton(ProjectIssueTypeContextImpl.createGlobalContext());
    }

}
