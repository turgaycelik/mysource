package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import webwork.action.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v4.0
 */
@Deprecated
public class VersionCustomFieldRenderer extends CustomFieldRenderer implements SearchRenderer
{
    private final VersionManager versionManager;

    public VersionCustomFieldRenderer(final ClauseNames clauseNames, final VersionManager versionManager,
            final FieldVisibilityManager fieldVisibilityManager, final CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
            final CustomFieldValueProvider customFieldValueProvider, final CustomField field)
    {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
        this.versionManager = versionManager;
    }

    @Override
    public boolean isShown(User user, SearchContext searchContext)
    {
        return super.isShown(user, searchContext) && isSingleProject(searchContext);
    }

    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        if (isSingleProject(searchContext))
        {
            HashMap<String, Object> velocityParameters = new HashMap<String, Object>();

            Project project = searchContext.getSingleProject();
            // JRA-15007: released versions must always be reversed (descending order)
            Collection releasedversion = versionManager.getVersionsReleasedDesc(project.getId(), false);
            Collection unreleasedversion = versionManager.getVersionsUnreleased(project.getId(), false);

            velocityParameters.put("releasedVersion", releasedversion);
            velocityParameters.put("unreleasedVersion", unreleasedversion);

            velocityParameters.put("multiple", Boolean.TRUE);

            return super.getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParameters);
        }
        else
        {
            return "";
        }
    }

    private boolean isSingleProject(SearchContext searchContext)
    {
        return searchContext != null && searchContext.isSingleProjectContext();
    }
}
