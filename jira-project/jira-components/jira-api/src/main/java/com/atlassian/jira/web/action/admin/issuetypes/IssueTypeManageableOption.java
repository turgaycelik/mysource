package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class IssueTypeManageableOption implements ManageableOptionType
{
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final ApplicationProperties properties;
    private JiraAuthenticationContext authenticationContext;

    public IssueTypeManageableOption(ConstantsManager constantsManager, SubTaskManager subTaskManager, ApplicationProperties properties, JiraAuthenticationContext authenticationContext)
    {
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.properties = properties;
        this.authenticationContext = authenticationContext;
    }

    public String getFieldId()
    {
        return IssueFieldConstants.ISSUE_TYPE;
    }

    public String getActionPrefix()
    {
        return "IssueType";
    }

    public String getLocalHelpSuffix()
    {
        return "IssueTypes";
    }

    public String getTitle()
    {
        return authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.title");
    }

    public String getTitleSingle()
    {
        return authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.title.single");
    }

    public String getTitleLowerCase()
    {
        return getTitle().toLowerCase();
    }

    public boolean isIconEnabled()
    {
        return true;
    }

    public boolean isTypeEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public Collection getAllOptions()
    {
        return constantsManager.getAllIssueTypeObjects();
    }

    public boolean isDefault(GenericValue constant)
    {
        String constantId = properties.getString(APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE);
        return (constantId != null && constant.getString("id").equals(constantId));
    }

    public Collection getTypesList()
    {
        return EasyList.build(new TextOption("", authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.standard.value"), authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.standard.desc")),
                             new TextOption(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.subtask.value"), authenticationContext.getI18nHelper().getText("admin.issue.type.manageable.option.subtask.desc")));
    }
}
