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

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultCustomFieldFactory implements CustomFieldFactory
{
    private final JiraAuthenticationContext authenticationContext;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final PermissionManager permissionManager;
    private final RendererManager rendererManager;
    private final FieldConfigSchemeClauseContextUtil contextUtil;
    private final CustomFieldDescription customFieldDescription;
    private final FeatureManager featureManager;
    private final TranslationManager translationManager;
    private final CustomFieldScopeFactory scopeFactory;
    private final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors;
    private final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors;

    public DefaultCustomFieldFactory(
            final JiraAuthenticationContext authenticationContext,
            final FieldConfigSchemeManager fieldConfigSchemeManager,
            final PermissionManager permissionManager,
            final RendererManager rendererManager,
            final FieldConfigSchemeClauseContextUtil contextUtil,
            final CustomFieldDescription customFieldDescription,
            final FeatureManager featureManager,
            final TranslationManager translationManager,
            final CustomFieldScopeFactory scopeFactory,
            final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors,
            final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors)
    {

        this.authenticationContext = authenticationContext;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
        this.rendererManager = rendererManager;
        this.contextUtil = contextUtil;
        this.customFieldDescription = customFieldDescription;
        this.featureManager = featureManager;
        this.translationManager = translationManager;
        this.scopeFactory = scopeFactory;
        this.customFieldTypeModuleDescriptors = customFieldTypeModuleDescriptors;
        this.customFieldSearcherModuleDescriptors = customFieldSearcherModuleDescriptors;
    }

    @Override
    public CustomField create(final GenericValue genericValue)
    {
        checkNotNull(genericValue);
        return new CustomFieldImpl(
                genericValue,
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
    public CustomField copyOf(final CustomField customField)
    {
        checkNotNull(customField);
        return new CustomFieldImpl(
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
}
