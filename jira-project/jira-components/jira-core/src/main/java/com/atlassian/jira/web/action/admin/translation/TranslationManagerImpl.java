/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.translation;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;

public class TranslationManagerImpl implements TranslationManager
{
    public static final String JIRA_ISSUETYPE_TRANSLATION_PREFIX = "jira.translation.issuetype";
    public static final String JIRA_PRIORITY_TRANSLATION_PREFIX = "jira.translation.priority";
    public static final String JIRA_RESOLUTION_TRANSLATION_PREFIX = "jira.translation.resolution";
    public static final String JIRA_STATUS_TRANSLATION_PREFIX = "jira.translation.status";
    public static final String JIRA_CF_TRANSLATION_PREFIX = "jira.translation.custom.field";

    public static final String NONE = "None";

    private final Map translationPrefixMap;
    private final JiraAuthenticationContext authenticationContext;
    private ApplicationProperties applicationProperties;
    private I18nHelper.BeanFactory beanFactory;

    public TranslationManagerImpl(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, I18nHelper.BeanFactory beanFactory)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.beanFactory = beanFactory;
        translationPrefixMap = new HashMap();
        translationPrefixMap.put("IssueType", JIRA_ISSUETYPE_TRANSLATION_PREFIX);
        translationPrefixMap.put("Priority", JIRA_PRIORITY_TRANSLATION_PREFIX);
        translationPrefixMap.put("Resolution", JIRA_RESOLUTION_TRANSLATION_PREFIX);
        translationPrefixMap.put("Status", JIRA_STATUS_TRANSLATION_PREFIX);
    }

    /**
     * Retrieves the installed locales in the user's language.
     * @return A map containing the installed locales indexed by each locale's string representation.
     */
    public Map getInstalledLocales()
    {
        Map locales = new ListOrderedMap();
        final List installedLocales = ManagerFactory.getJiraLocaleUtils().getInstalledLocales();
        for (int i = 0; i < installedLocales.size(); i++)
        {
            Locale locale = (Locale) installedLocales.get(i);
            locales.put(locale.toString(), locale.getDisplayName(authenticationContext.getLocale()));
        }

        return locales;
    }

    public String getTranslatedNameFromString(String translationString)
    {
        StringTokenizer st = new StringTokenizer(translationString, "\n");
        if (st.countTokens() == 2)
        {
            return (String) st.nextElement();
        }
        return null;
    }

    public String getTranslatedDescriptionFromString(String translationString)
    {
        StringTokenizer st = new StringTokenizer(translationString, "\n");
        String extractedDesc = null;
        while (st.hasMoreElements())
        {
            extractedDesc = (String) st.nextElement();
        }
        return extractedDesc;
    }

    /**
     * Extract the desired string (name/description) from the specified issue constant.
     * <p>
     * If a system defined translation does not exist, the property file associated with the i18nHelper is checked.
     *
     * @param issueConstant
     * @param name      boolean - fetch name or description
     * @param locale    used to check if system defined property exists
     * @param i18n      the i18nHelper to use to retrieve the translation from property files if no defined within system
     * @return String   translated issue constant name or description
     */
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale, I18nHelper i18n)
    {
        String translation = getTranslationFromDatabase(issueConstant, locale, name);
        if (TextUtils.stringSet(translation))
        {
            return translation;
        }

        if (TextUtils.stringSet(getTranslationPrefix(issueConstant)))
        {
            // If we have a JIRA instance set in English, if the admin changes
            // the default name/description, it will probably be a new name/description in English.
            //
            // In this situation, if the translation is requested for a French user,
            // we default to the translation on the i18n resources, so the French user always sees
            // a French name/description (instead of giving him the message in English that was set
            // by the admin)
            if (isLocaleDifferentThanDefault(locale))
            {
                return getTranslationFromI18nResources(issueConstant, i18n, name);
            }

            if (valueOnIssueConstantIsTheDefault(issueConstant, name))
            {
                return getTranslationFromI18nResources(issueConstant, i18n, name);
            }
        }

        return name ? issueConstant.getName() : issueConstant.getDescription();
    }

    private String getTranslationFromDatabase(IssueConstant issueConstant, String locale, boolean name)
    {
        PropertySet ps = issueConstant.getPropertySet();

        String translationPrefix = getTranslationPrefix(issueConstant);
        String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);

        if (TextUtils.stringSet(translationString))
        {
            return name ? getTranslatedNameFromString(translationString) : getTranslatedDescriptionFromString(translationString);
        }
        return null;
    }

    private boolean isLocaleDifferentThanDefault(String locale)
    {
        return !applicationProperties.getDefaultLocale().toString().equals(locale);
    }

    private String getTranslationFromI18nResources(IssueConstant issueConstant, I18nHelper i18n, boolean name)
    {
        if (i18n == null)
        {
            i18n = authenticationContext.getI18nHelper();
        }

        String key = getI18NTranslationKey(issueConstant, name);
        String translation = i18n.getText(key);

        if (translation != null && !translation.contains(key))
        {
            return translation;
        }

        return name ? issueConstant.getName() : issueConstant.getDescription();
    }

    private boolean valueOnIssueConstantIsTheDefault(IssueConstant issueConstant, boolean name)
    {
        I18nHelper defaultI18n = beanFactory.getInstance(Locale.ROOT);
        String key = getI18NTranslationKey(issueConstant, name);
        String defaultValue = defaultI18n.getResourceBundle().containsKey(key) ? defaultI18n.getResourceBundle().getString(key) : null;

        if (name)
        {
            return issueConstant.getName() != null && issueConstant.getName().equals(defaultValue);
        }
        return issueConstant.getDescription() != null && issueConstant.getDescription().equals(defaultValue);
    }

    private String getI18NTranslationKey(IssueConstant issueConstant, boolean name)
    {
        String translationPrefix = getTranslationPrefix(issueConstant);
        return translationPrefix + "." + makeNameIntoProperty(issueConstant.getName()) + "." + (name ? "name" : "desc");
    }

    // Extract the desired string (name/description) from the specified issue constant
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale)
    {
        return getIssueConstantTranslation(issueConstant, name, locale, null);
    }

    public boolean hasLocaleTranslation(IssueConstant issueConstant, String locale)
    {
        PropertySet ps = issueConstant.getPropertySet();

        String issueConstantType = issueConstant.getGenericValue().getEntityName();
        String translationPrefix = getTranslationPrefix(issueConstantType);

        String translationString = ps.getString(translationPrefix + issueConstant.getId() + "." + locale);

        return TextUtils.stringSet(translationString);
    }

    private String makeNameIntoProperty(String issueConstantName)
    {
        return StringUtils.deleteWhitespace(issueConstantName).toLowerCase();
    }

    private String getTranslationPrefix(String issueConstantType)
    {
        return (String) translationPrefixMap.get(issueConstantType);
    }

    private String getTranslationPrefix(IssueConstant issueConstant)
    {
        return getTranslationPrefix(issueConstant.getGenericValue().getEntityName());
    }

    // Extract the desired string (name/description) from the specified issue constant
    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, Locale locale)
    {
        return getIssueConstantTranslation(issueConstant, name, locale.toString());
    }

    public void setIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale, String translatedName, String translatedDesc)
    {
        PropertySet ps = issueConstant.getPropertySet();
        String issueConstantLocaleKey = issueConstantPrefix + issueConstant.getId() + "." + locale;

        // Set property
        if (TextUtils.stringSet(translatedName) && TextUtils.stringSet(translatedDesc))
            ps.setString(issueConstantLocaleKey, translatedName + "\n" + translatedDesc);

        // Can't inject this due to circular dependency
        ComponentAccessor.getConstantsManager().invalidate(issueConstant);
    }

    public void deleteIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale)
    {
        PropertySet ps = issueConstant.getPropertySet();
        String issueConstantLocaleKey = issueConstantPrefix + issueConstant.getId() + "." + locale;

        if (ps.exists(issueConstantLocaleKey))
            ps.remove(issueConstantLocaleKey);

        // Can't inject this due to circular dependency
        ComponentAccessor.getConstantsManager().invalidate(issueConstant);
    }

    @Override
    public String getCustomFieldNameTranslation(final CustomField customField)
    {
        return getCustomFieldNameTranslation(customField, authenticationContext.getLocale());
    }

    @Override
    public String getCustomFieldNameTranslation(final CustomField customField, final Locale locale)
    {
        PropertySet ps = customField.getPropertySet();
        String nameKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "name" + "." + locale;
        return ps.getString(nameKey);
    }

    @Override
    public String getCustomFieldDescriptionTranslation(final CustomField customField)
    {
        return getCustomFieldDescriptionTranslation(customField, authenticationContext.getLocale());
    }

    @Override
    public String getCustomFieldDescriptionTranslation(final CustomField customField, final Locale locale)
    {
        PropertySet ps = customField.getPropertySet();
        String descKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "desc" + "." + locale;
        return ps.getString(descKey);
    }

    @Override
    public void setCustomFieldTranslation(final CustomField customField, final Locale locale, final String translatedName, final String translatedDesc)
    {
        PropertySet ps = customField.getPropertySet();
        String nameKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "name" + "." + locale;
        if (TextUtils.stringSet(translatedName))
        {
            ps.setString(nameKey, translatedName);
        }
        else
        {
            if (ps.getString(nameKey) != null)
            {
                ps.remove(nameKey);
            }
        }
        String descKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "desc" + "." + locale;
        if (TextUtils.stringSet(translatedDesc))
        {
            ps.setString(descKey, translatedDesc);
        }
        else
        {
            if (ps.getString(descKey) != null)
            {
                ps.remove(descKey);
            }
        }
    }

    @Override
    public void deleteCustomFieldTranslation(final CustomField customField, final Locale locale)
    {
        PropertySet ps = customField.getPropertySet();
        String nameKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "name" + "." + locale;
        if (ps.exists(nameKey))
        {
            ps.remove(nameKey);
        }
        String descKey = JIRA_CF_TRANSLATION_PREFIX + customField.getId() + "." + "desc" + "." + locale;
        if (ps.exists(descKey))
        {
            ps.remove(descKey);
        }
    }
}
