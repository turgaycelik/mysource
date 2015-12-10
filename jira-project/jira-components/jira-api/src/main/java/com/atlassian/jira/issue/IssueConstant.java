/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import java.util.Locale;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.I18nHelper;

import com.opensymphony.module.propertyset.PropertySet;

import org.ofbiz.core.entity.GenericValue;

/**
 * Abstraction to represent any of the various constants like {@link com.atlassian.jira.issue.resolution.Resolution},
 * {@link com.atlassian.jira.issue.status.Status} etc.
 */
@PublicApi
public interface IssueConstant extends Comparable
{
    GenericValue getGenericValue();

    String getId();

    String getName();

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void setName(String name);

    String getDescription();

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void setDescription(String description);

    Long getSequence();

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void setSequence(Long sequence);

    /**
     * Returns the url for the icon with the context path added if necessary.
     *
     * @return the url for the icon with the context path added if necessary. null will be returned if there
     * is no icon URL.
     */
    String getCompleteIconUrl();

    String getIconUrl();

    /**
     * Returns the HTML-escaped URL for this issue constant.
     *
     * @return a String containing an HTML-escaped icon URL
     * @see #getIconUrl()
     */
    String getIconUrlHtml();

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void setIconUrl(String iconURL);

    // Retrieve name translation in current locale
    String getNameTranslation();

    // Retrieve desc translation in current locale
    String getDescTranslation();

    // Retrieve name translation in specified locale
    String getNameTranslation(String locale);

    // Retrieve desc translation in specified locale
    String getDescTranslation(String locale);

    String getNameTranslation(I18nHelper i18n);

    String getDescTranslation(I18nHelper i18n);

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void setTranslation(String translatedName, String translatedDesc, String issueConstantPrefix, Locale locale);

    /** @deprecated since v6.2. All updates should be performed via the relevant manager. */
    @Deprecated
    void deleteTranslation(String issueConstantPrefix, Locale locale);

    PropertySet getPropertySet();
}
