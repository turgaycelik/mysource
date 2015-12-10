package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericValue;

/**
 * The only purpose of this class is to increase the visibility of #copyGenericValue so we can mock it in tests.
 */
public class CustomFieldTestImpl extends CustomFieldImpl
{
    public CustomFieldTestImpl(
            CustomField customField,
            JiraAuthenticationContext authenticationContext,
            FieldConfigSchemeManager fieldConfigSchemeManager,
            PermissionManager permissionManager,
            RendererManager rendererManager,
            FieldConfigSchemeClauseContextUtil contextUtil,
            CustomFieldDescription customFieldDescription,
            FeatureManager featureManager,
            TranslationManager translationManager,
            CustomFieldScopeFactory scopeFactory,
            CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors,
            CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors)
    {
        super(
                customField,
                authenticationContext,
                fieldConfigSchemeManager,
                permissionManager,
                rendererManager,
                contextUtil,
                customFieldDescription,
                featureManager,
                translationManager,
                scopeFactory,
                customFieldTypeModuleDescriptors,
                customFieldSearcherModuleDescriptors
        );
    }

    @Override
    public GenericValue copyGenericValue()
    {
        return super.copyGenericValue();
    }
}
