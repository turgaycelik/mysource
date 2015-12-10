/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.stripToNull;

public class IssueConstantImpl extends AbstractOfBizValueWrapper implements IssueConstant
{
    private final TranslationManager translationManager;
    private final JiraAuthenticationContext authenticationContext;
    private final BaseUrl locator;
    private final LazyReference<PropertySet> propertySetRef = new LazyReference<PropertySet>()
    {
        @Override
        protected PropertySet create() throws Exception
        {
            return OFBizPropertyUtils.getCachingPropertySet(genericValue.getEntityName(), new Long(genericValue.getString("id")));
        }
    };

    // Cache for name translations - These are safe as all cached Issue Constants are purged when the constants are updated
    @ClusterSafe
    private final Map<String, String> nameTranslations = new MapMaker()
            .concurrencyLevel(1)
            .makeComputingMap(new IssueConstantTranslationFunction(true));
    // Cache for description translations - These are safe as all cached Issue Constants are purged when the constants are updated
    @ClusterSafe
    private final Map<String, String> descriptionTranslations = new MapMaker()
            .concurrencyLevel(1)
            .makeComputingMap(new IssueConstantTranslationFunction(false));
    // perf: cache this as it gets called a lot in lucene sorts - These are safe as all cached Issue Constants are purged when the constants are updated
    @ClusterSafe
    private final ResettableLazyReference<Long> sequence = new ResettableLazyReference<Long>()
    {
        @Override
        protected Long create() throws Exception
        {
            return genericValue.getLong("sequence");
        }
    };

    public IssueConstantImpl(GenericValue genericValue, TranslationManager translationManager, JiraAuthenticationContext authenticationContext, BaseUrl locator)
    {
        super(genericValue);
        this.translationManager = translationManager;
        this.authenticationContext = authenticationContext;
        this.locator = locator;
    }

    public final String getId()
    {
        return genericValue.getString("id");
    }

    public final String getName()
    {
        return genericValue.getString("name");
    }

    public final void setName(String name)
    {
        genericValue.setString("name", name);
    }

    public final String getDescription()
    {
        return genericValue.getString("description");
    }

    public final void setDescription(String description)
    {
        genericValue.setString("description", description);
    }

    public final Long getSequence()
    {
        return sequence.get();
    }

    public final void setSequence(final Long seq)
    {
        genericValue.set("sequence", seq);
        sequence.reset();
    }

    @Override
    public String getCompleteIconUrl()
    {
        String iconUrl = stripToNull(getIconUrl());
        if (iconUrl == null)
        {
            return null;
        }
        else if (iconUrl.startsWith("http://") || iconUrl.startsWith("https://"))
        {
            return iconUrl;
        }
        else
        {
            return locator.getBaseUrl() + iconUrl;
        }
    }

    public String getIconUrl()
    {
        return genericValue.getString("iconurl");
    }

    public String getIconUrlHtml()
    {
        return StringEscapeUtils.escapeHtml(getIconUrl());
    }

    public void setIconUrl(String iconURL)
    {
        genericValue.setString("iconurl", iconURL);
    }

    /** Retrieve name translation in current locale */
    public final String getNameTranslation()
    {
        return nameTranslations.get(authenticationContext.getLocale().toString());
    }

    /** Retrieve desc translation in current locale */
    public final String getDescTranslation()
    {
        return descriptionTranslations.get(authenticationContext.getLocale().toString());
    }

    /** Retrieve name translation in specified locale */
    public final String getNameTranslation(String locale)
    {
        return nameTranslations.get(locale);
    }

    /** Retrieve desc translation in specified locale */
    public final String getDescTranslation(String locale)
    {
        return descriptionTranslations.get(locale);
    }

    /**
     * Retrieve name translation.
     * <p>
     * If a system defined translation does not exist, the property files for the locale are checked.
     * </p>
     *
     * @param i18n an I18nHelper to use for the translation.
     * @return String   translated name
     */
    public final String getNameTranslation(I18nHelper i18n)
    {
        return translationManager.getIssueConstantTranslation(this, true, i18n.getLocale().toString(), i18n);
    }

    /**
     * Retrieve desc translation.
     * <p>
     * If a system defined translation does not exist, the property files for the locale are checked.
     * </p>
     *
     * @param i18n an I18nHelper to use for the translation.
     * @return String   translated description
     */
    public final String getDescTranslation(I18nHelper i18n)
    {
        return translationManager.getIssueConstantTranslation(this, false, i18n.getLocale().toString(), i18n);
    }

    public final void setTranslation(String translatedName, String translatedDesc, String issueConstantPrefix, Locale locale)
    {
        translationManager.setIssueConstantTranslation(this, issueConstantPrefix, locale, translatedName, translatedDesc);
        nameTranslations.remove(locale.toString());
        descriptionTranslations.remove(locale.toString());
    }

    public final void deleteTranslation(String issueConstantPrefix, Locale locale)
    {
        translationManager.deleteIssueConstantTranslation(this, issueConstantPrefix, locale);
        nameTranslations.remove(locale.toString());
        descriptionTranslations.remove(locale.toString());
    }

    public final int compareTo(Object o)
    {
        return genericValue.compareTo(((IssueConstant) o).getGenericValue());
    }

    public PropertySet getPropertySet()
    {
        return propertySetRef.get();
    }

    private class IssueConstantTranslationFunction implements Function<String, String>
    {
        private final boolean isName;

        private IssueConstantTranslationFunction(boolean isName)
        {
            super();
            this.isName = isName;
        }

        public String apply(String key)
        {
            String translation = null;
            Locale locale = LocaleParser.parseLocale(key);
            if (locale != null)
            {
                translation = translationManager.getIssueConstantTranslation(IssueConstantImpl.this, isName, locale);
            }
            return translation == null ? "" : translation;
        }
    }
}
