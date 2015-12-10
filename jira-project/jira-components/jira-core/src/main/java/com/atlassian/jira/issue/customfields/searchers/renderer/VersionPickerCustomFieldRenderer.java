package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractVersionRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * {@link SearchRenderer} for the custom fields that allow to pick versions.
 */
public class VersionPickerCustomFieldRenderer extends AbstractVersionRenderer
{
    private CustomField customField;
    private FieldVisibilityManager fieldVisibilityManager;

    public VersionPickerCustomFieldRenderer(
            final CustomField customField,
            final ProjectManager projectManager,
            final VersionManager versionManager,
            final FieldVisibilityManager fieldVisibilityManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties,
            final VelocityTemplatingEngine templatingEngine,
            final PermissionManager permissionManager,
            final String searcherNameKey)
    {
        super(fieldSearchConstants(customField), searcherNameKey, projectManager, versionManager, velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, permissionManager, true);
        this.customField = customField;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    private static SimpleFieldSearchConstantsWithEmpty fieldSearchConstants(final CustomField customField)
    {
        String id = customField.getId();
        return new SimpleFieldSearchConstantsWithEmpty(
                id, // indexField
                customField.getClauseNames(), // clauseNames
                id, // urlParameter
                id, // searcherId
                VersionManager.NO_VERSIONS, // emptySelectFlag
                FieldIndexer.NO_VALUE_INDEX_VALUE, // emptyIndexValue
                id, // fieldId
                OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, // supportedOperators
                JiraDataTypes.VERSION // supported type
        );
    }

    @Override
    public boolean isShown(User user, SearchContext searchContext)
    {
        return hasAnyValidOption(user, searchContext) && CustomFieldUtils.isShownAndVisible(customField, user, searchContext, fieldVisibilityManager);
    }
}
