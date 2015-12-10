/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.translation;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;
import java.util.Map;

public interface TranslationManager
{
    public Map getInstalledLocales();

    public String getTranslatedNameFromString(String translationString);

    public String getTranslatedDescriptionFromString(String translationString);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, String locale, I18nHelper i18n);

    public String getIssueConstantTranslation(IssueConstant issueConstant, boolean name, Locale locale);

    public boolean hasLocaleTranslation(IssueConstant issueConstant, String locale);

    public void setIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale, String translatedName, String translatedDesc);

    public void deleteIssueConstantTranslation(IssueConstant issueConstant, String issueConstantPrefix, Locale locale);

    /**
     * Get the translated name for the custom field or null if no translation exists.
     * This will use the current authentication context
     * @param customField a custom field
     * @return the translated name
     */
    public String getCustomFieldNameTranslation(CustomField customField);

    /**
     * Get the translated name for the custom field or null if no translation exists.
     * @param customField a custom field
     * @param locale The locale to get the translation for.
     * @return the translated name
     */
    public String getCustomFieldNameTranslation(CustomField customField, Locale locale);

    /**
     * Get the translated name for the custom field or null if no translation exists.
     * This will use the current authentication context
     * @param customField a custom field
     * @return the translated name
     */
    public String getCustomFieldDescriptionTranslation(CustomField customField);

    /**
     * Get the translated name for the custom field or null if no translation exists.
     * @param customField a custom field
     * @param locale The locale to get the translation for.
     * @return the translated name
     */
    public String getCustomFieldDescriptionTranslation(CustomField customField, Locale locale);

    /**
     * Store the translated name and description for a custom field in a particular locale
     * @param customField a custom field
     * @param locale the locale
     * @param translatedName the translated name
     * @param translatedDesc the translated description
     */
    public void setCustomFieldTranslation(CustomField customField, Locale locale, String translatedName, String translatedDesc);

    public void deleteCustomFieldTranslation(CustomField customField, Locale locale);

}
