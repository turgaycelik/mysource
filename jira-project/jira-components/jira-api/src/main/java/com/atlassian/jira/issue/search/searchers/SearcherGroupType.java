package com.atlassian.jira.issue.search.searchers;

import com.atlassian.annotations.PublicApi;

/**
 * Represents the type of a {@link com.atlassian.jira.issue.search.searchers.SearcherGroup}.
 */
@PublicApi
public enum SearcherGroupType
{
    TEXT
    (
//        "navigator.filter.subheading.textsearch",
            null
    ),

    CONTEXT
    (
        null
    ),

    PROJECT
    (
        "common.concepts.projectcomponents"
    ),

    ISSUE
    (
        "navigator.filter.subheading.issueattributes"
    ),

    DATE
    (
        "navigator.filter.subheading.datesandtimes"
    ),

    WORK
    (
        "navigator.filter.subheading.workratio"
    ),

    CUSTOM
    (
        "navigator.filter.subheading.customfields"
    );

    private final String i18nKey;

    private SearcherGroupType(final String key)
    {
        this.i18nKey = key;
    }

    public String getI18nKey()
    {
        return i18nKey;
    }

}
