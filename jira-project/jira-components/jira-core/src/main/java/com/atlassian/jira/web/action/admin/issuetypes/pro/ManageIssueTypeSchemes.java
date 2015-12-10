package com.atlassian.jira.web.action.admin.issuetypes.pro;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.List;

@WebSudoRequired
public class ManageIssueTypeSchemes extends AbstractManageIssueTypeOptionsAction
{
    private final TranslationManager translationManager;

    public ManageIssueTypeSchemes(FieldConfigSchemeManager configSchemeManager,
            IssueTypeSchemeManager issueTypeSchemeManager, FieldManager fieldManager, OptionSetManager optionSetManager,
            IssueTypeManageableOption manageableOptionType, BulkMoveOperation bulkMoveOperation,
            SearchProvider searchProvider, IssueManager issueManager, TranslationManager translationManager)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType,
                bulkMoveOperation, searchProvider, issueManager);
        this.translationManager = translationManager;
    }

    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected String doExecute() throws Exception
    {
        return super.doExecute();
    }

    public String doAddScheme() throws Exception
    {
        return SUCCESS;
    }

    public List<FieldConfigScheme> getSchemes()
    {
        final ConfigurableField field = getConfigurableField();
        return configSchemeManager.getConfigSchemesForField(field);
    }

    public boolean isDefault(String id, FieldConfigScheme configScheme)
    {
        IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(configScheme.getOneAndOnlyConfig());
        return defaultValue != null && id.equals(defaultValue.getId());
    }

    public String getActionType()
    {
        return "scheme";
    }

    public Collection getOptions(FieldConfigScheme configScheme)
    {
        final FieldConfig config = configScheme.getOneAndOnlyConfig();
        return optionSetManager.getOptionsForConfig(config).getOptions();
    }

    public boolean isDefault(FieldConfigScheme configScheme)
    {
        return issueTypeSchemeManager.isDefaultIssueTypeScheme(configScheme);
    }

    public boolean isTranslatable()
    {
        //JRA-16912: Only show the 'Translate' link if there's any installed languages to translate to!
        return !translationManager.getInstalledLocales().isEmpty();
    }
}