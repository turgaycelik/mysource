package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class TranslateCustomField extends JiraWebActionSupport
{
    private Long id;
    private String name;
    private String description;
    private String selectedLocale;

    private final CustomFieldManager customFieldManager;
    private final CustomFieldService customFieldService;
    private final ManagedConfigurationItemService managedConfigurationItemService;
    private final LocaleManager localeManager;
    private final TranslationManager translationManager;
    private final CustomFieldDescription customFieldDescription;

    public TranslateCustomField(CustomFieldService customFieldService, CustomFieldManager customFieldManager,
            ManagedConfigurationItemService managedConfigurationItemService, final LocaleManager localeManager, final TranslationManager translationManager, final CustomFieldDescription customFieldDescription)
    {
        this.customFieldService = customFieldService;
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.localeManager = localeManager;
        this.translationManager = translationManager;
        this.customFieldDescription = customFieldDescription;
    }

    public String doDefault() throws Exception
    {
        if (!validateFieldLocked())
        {
            return Action.ERROR;
        }

        Locale locale;
        if (StringUtils.isNotEmpty(getSelectedLocale()))
        {
            locale = localeManager.getLocale(getSelectedLocale());
        }
        else
        {
            locale = getLocale();
        }
        if (locale != null)
        {
            setName(translationManager.getCustomFieldNameTranslation(getCustomField(), locale));
            setDescription(translationManager.getCustomFieldDescriptionTranslation(getCustomField(), locale));
            setSelectedLocale(locale.toString());
        }

        return Action.INPUT;
    }

    protected void doValidation()
    {
        if (!validateFieldLocked())
        {
            return;
        }
        customFieldService.validateTranslation(getJiraServiceContext(), id, name, description, getSelectedLocale());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        customFieldService.updateTranslation(getJiraServiceContext(), id, name, description, getSelectedLocale());

        return getRedirect("TranslateCustomField!default.jspa?id=" + getId() + "&selectedLocale=" + getSelectedLocale());
    }

    public boolean isFieldLocked()
    {
        return !managedConfigurationItemService.doesUserHavePermission(getLoggedInUser(), getManagedConfigurationEntity());
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

    public RenderableProperty getDescriptionProperty()
    {
        return customFieldDescription.createRenderablePropertyFor(getDescription());
    }

    public String getSelectedLocale()
    {
        return selectedLocale;
    }

    public void setSelectedLocale(final String selectedLocale)
    {
        this.selectedLocale = selectedLocale;
    }

    // Retrieve the installed locales
    public Map getInstalledLocales()
    {
        return translationManager.getInstalledLocales();
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getId());
    }
}