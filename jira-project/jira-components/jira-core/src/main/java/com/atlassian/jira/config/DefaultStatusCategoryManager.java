package com.atlassian.jira.config;

import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryImpl;
import com.atlassian.jira.plugin.profile.DarkFeatures;

import java.util.List;

/**
 * @since v6.1
 */
public class DefaultStatusCategoryManager implements StatusCategoryManager
{

    public static final String JIRA_ISSUE_STATUS_AS_LOZENGE_FEATURE = "jira.issue.status.lozenge";

    private final FeatureManager featureManager;

    public DefaultStatusCategoryManager(final FeatureManager featureManager) {this.featureManager = featureManager;}

    @Override
    public List<StatusCategory> getStatusCategories()
    {
        return StatusCategoryImpl.getAllCategories();
    }

    @Override
    public List<StatusCategory> getUserVisibleStatusCategories()
    {
        return StatusCategoryImpl.getUserVisibleCategories();
    }

    @Override
    public StatusCategory getDefaultStatusCategory()
    {
        return StatusCategoryImpl.getDefault();
    }

    @Override
    public StatusCategory getStatusCategory(final Long id)
    {
        return StatusCategoryImpl.findById(id);
    }

    @Override
    public StatusCategory getStatusCategoryByKey(final String key)
    {
        return StatusCategoryImpl.findByKey(key);
    }

    @Override
    public StatusCategory getStatusCategoryByName(String name)
    {
        return StatusCategoryImpl.findByName(name);
    }

    @Override
    public boolean isStatusAsLozengeEnabled()
    {
        final DarkFeatures darkFeatures = featureManager.getDarkFeatures();
        return darkFeatures.isFeatureEnabled(JIRA_ISSUE_STATUS_AS_LOZENGE_FEATURE);
    }
}
