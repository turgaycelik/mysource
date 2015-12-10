package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.Action;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class EditCustomField extends JiraWebActionSupport
{
    private Long id;
    private String name;
    private String description;
    private String searcher;

    private final CustomFieldManager customFieldManager;
    private final CustomFieldService customFieldService;
    private final ReindexMessageManager reindexMessageManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    public EditCustomField(CustomFieldService customFieldService, CustomFieldManager customFieldManager, final ReindexMessageManager reindexMessageManager,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.customFieldService = customFieldService;
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
    }

    public String doDefault() throws Exception
    {
        if (!validateFieldLocked())
        {
            return Action.ERROR;
        }

        setName(getCustomField().getUntranslatedName());
        setDescription(getCustomField().getUntranslatedDescription());
        final CustomFieldSearcher currentSearcher = getCurrentSearcher();
        setSearcher(currentSearcher != null ? currentSearcher.getDescriptor().getCompleteKey() : null);
        return Action.INPUT;
    }

    protected void doValidation()
    {
        if (!validateFieldLocked())
        {
            return;
        }
        customFieldService.validateUpdate(getJiraServiceContext(), id, name, description, searcher);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        CustomField updatedField = getCustomField();
        updatedField.setName(getName());
        updatedField.setDescription(getDescription());
        final CustomFieldSearcher oldSearcher = updatedField.getCustomFieldSearcher();
        if (ObjectUtils.isValueSelected(searcher))
        {
            final CustomFieldSearcher newSearcher = customFieldManager.getCustomFieldSearcher(searcher);
            updatedField.setCustomFieldSearcher(newSearcher);

            // if searcher has changed, then we need to push a reindex message
            if (oldSearcher == null || !oldSearcher.getDescriptor().getCompleteKey().equals(newSearcher.getDescriptor().getCompleteKey()))
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.custom.fields");
            }
        }
        else
        {
            updatedField.setCustomFieldSearcher(null);
        }
        customFieldManager.updateCustomField(updatedField);

        return getRedirect("ViewCustomFields.jspa");
    }

    public boolean isFieldManaged()
    {
        return getManagedConfigurationEntity().isManaged();
    }

    public boolean isFieldLocked()
    {
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), getManagedConfigurationEntity());
    }

    public String getManagedFieldDescriptionKey()
    {
        return getManagedConfigurationEntity().getDescriptionI18nKey();
    }

    private boolean validateFieldLocked()
    {
        if (isFieldLocked())
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.edit.locked", getCustomField().getName()));
            return false;
        }
        return true;
    }

    private ManagedConfigurationItem getManagedConfigurationEntity()
    {
        CustomField customField = getCustomField();
        if (customField == null)
        {
            return null;
        }
        return managedConfigurationItemService.getManagedCustomField(customField);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long l)
    {
        id = l;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setSearcher(String searcher)
    {
        this.searcher = searcher;
    }

    public String getSearcher()
    {
        return searcher;
    }

    public List getSearchers()
    {
        return customFieldManager.getCustomFieldSearchers((customFieldManager.getCustomFieldObject(getId()).getCustomFieldType()));
    }

    public CustomFieldSearcher getCurrentSearcher()
    {
        return getCustomField().getCustomFieldSearcher();
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getId());
    }
}