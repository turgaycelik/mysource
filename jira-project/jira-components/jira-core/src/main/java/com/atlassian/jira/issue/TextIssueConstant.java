package com.atlassian.jira.issue;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringEscapeUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Locale;

public class TextIssueConstant implements IssueConstant
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Logger log = Logger.getLogger(TextIssueConstant.class);
    private static final Long SEQUENCE = new Long(0);

    // ------------------------------------------------------------------------------------------------- Type Properties
    private String nameKey;
    private String descriptionKey;
    private String iconUrl;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraAuthenticationContext authenticationContext;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public TextIssueConstant(String nameKey, String descriptionKey, String iconUrl, JiraAuthenticationContext authenticationContext)
    {
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.iconUrl = iconUrl;
        this.authenticationContext = authenticationContext;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public GenericValue getGenericValue()
    {
        handleException();
        return null;
    }

    public String getId()
    {
        return "-1";
    }

    public String getName()
    {
        return getI18n().getText(nameKey);
    }


    public void setName(String name)
    {
        handleException();
    }


    public String getDescription()
    {
        return getI18n().getText(descriptionKey);
    }

    public void setDescription(String description)
    {
        handleException();
    }

    public Long getSequence()
    {
        return SEQUENCE;
    }

    public void setSequence(Long sequence)
    {
        handleException();
    }

    @Override
    public String getCompleteIconUrl()
    {
        return getIconUrl();
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public String getIconUrlHtml()
    {
        return StringEscapeUtils.escapeHtml(getIconUrl());
    }

    public void setIconUrl(String iconURL)
    {
        handleException();
    }

    // Retrieve name translation in current locale
    public String getNameTranslation()
    {
        return getI18n().getText(nameKey);
    }

    // Retrieve desc translation in current locale
    public String getDescTranslation()
    {
        return getI18n().getText(descriptionKey);
    }

    // Retrieve name translation in specified locale
    public String getNameTranslation(String locale)
    {
        return new I18nBean(LocaleParser.parseLocale(locale)).getText(nameKey);
    }

    // Retrieve desc translation in specified locale
    public String getDescTranslation(String locale)
    {
        return new I18nBean(LocaleParser.parseLocale(locale)).getText(descriptionKey);
    }

    public String getNameTranslation(I18nHelper i18n)
    {
        return new I18nBean(i18n.getLocale()).getText(nameKey);
    }

    public String getDescTranslation(I18nHelper i18n)
    {
        return new I18nBean(i18n.getLocale()).getText(descriptionKey);
    }

    public void setTranslation(String translatedName, String translatedDesc, String issueConstantPrefix, Locale locale)
    {
        handleException();
    }

    public void deleteTranslation(String issueConstantPrefix, Locale locale)
    {
        handleException();
    }

    public PropertySet getPropertySet()
    {
        handleException();
        return null;
    }

    public int compareTo(Object o)
    {
        return 0;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    private I18nHelper getI18n()
    {
        return authenticationContext.getI18nHelper();
    }

    private void handleException()
    {
        log.warn("Unable to call method. Method not implemented");
        throw new IllegalArgumentException("Unable to call method. Method not implemented");
    }


}
