package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.CustomField;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Map;

public class CustomFieldContextManagementBean
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String BASIC_SCOPE_GLOBAL = "global";
    public static final String BASIC_SCOPE_PROJECT_CATEGORY = "projectcategory";
    public static final String BASIC_SCOPE_PROJECT = "project";
    public static final String BASIC_SCOPE_ISSUE_TYPE = "issuetype";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private Long customFieldId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected final CustomFieldManager customFieldManager;
    protected final JiraContextTreeManager treeManager;



    // ---------------------------------------------------------------------------------------------------- Constructors
    public CustomFieldContextManagementBean(CustomFieldManager customFieldManager, JiraContextTreeManager treeManager)
    {
        this.customFieldManager = customFieldManager;
        this.treeManager = treeManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public CustomField getCustomField()
    {
        CustomField customField = customFieldManager.getCustomFieldObject(getCustomFieldId());
        return customField;
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public void setCustomFieldId(Long customFieldId)
    {
        this.customFieldId = customFieldId;
    }
    
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    public static Map getGlobalContextOption()
    {
        // Always recreate this since the locale may change
        Map globalContextOptions = new ListOrderedMap();
        globalContextOptions.put("true", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("admin.global.context.option.global", "<strong>", "</strong>"));
        globalContextOptions.put("false", ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("admin.global.context.option.project"));
        return globalContextOptions;
    }
}
