package com.atlassian.jira.issue.status.category;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.I18nHelper;

import java.io.Serializable;

/**
 * Represents single group of {@link com.atlassian.jira.issue.status.Status} which has similar positions in terms of
 * representing in workflows.
 *
 * @since v6.1
 */
@PublicApi
public interface StatusCategory extends Serializable, Comparable<StatusCategory>
{
    Long getId();

    /**
     * Translated name for the status category.
     * Don't call this in a background thread! It'll fall back to the server's locale. Use {@link #getTranslatedName(com.atlassian.jira.util.I18nHelper)} instead.
     */
    String getTranslatedName();

    /**
     * Translated name for the status category.
     */
    String getTranslatedName(String locale);

    /**
     * Translated name for the status category.
     */
    String getTranslatedName(I18nHelper i18n);

    /**
     * Unique {@link String} identifier of given category. Should not contain any spaces and non-standard ASCII
     * characters.
     */
    String getKey();

    /**
     * I18n independent human readable name for the status category.<p>
     *
     * Used primarily for locale-independent JQL statements.
     *
     * @return Human readable name
     * @since v6.2
     */
    String getName();

    /**
     * Returns one of the 6 defined ADG colors: "medium-gray", "green", "yellow", "brown", "warm-red", "blue-gray"
     * @return
     */
    String getColorName();

    Long getSequence();

    public static String UNDEFINED = "undefined";
    public static String TO_DO = "new";
    public static String IN_PROGRESS = "indeterminate";
    public static String COMPLETE = "done";

}
