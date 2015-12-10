package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.customfields.searchers.*;
import com.atlassian.jira.issue.search.searchers.impl.*;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * Given an {@code IssueSearcher} class, returns information about the
 * corresponding panel on the view issue page. {@code AssigneeSearcher}, for
 * example, maps to {@code IssueSearcherPanelMap.Panel.PEOPLE}.
 *
 * @since v5.1
 */
public class IssueSearcherPanelMap
{
    public static enum Panel
    {
        DATES("common.concepts.dates"),
        DETAILS("viewissue.subheading.issuedetails"),
        PEOPLE("common.concepts.people");

        private final String titleKey;

        Panel(String titleKey)
        {
            this.titleKey = titleKey;
        }

        /**
         * @return The i18n key of the group's title.
         */
        public String getTitleKey()
        {
            return titleKey;
        }
    }

    // Maps IssueSearcher classes to the Panel they belong to. If neither a
    // class nor one of its superclasses is present, default to Panel.DETAILS.
    private static final ImmutableMap<Class<? extends IssueSearcher>, Panel> SEARCHER_PANEL_MAP;
    static
    {
        Map<Class<? extends IssueSearcher>, Panel> map = new HashMap<Class<? extends IssueSearcher>, Panel>();
        map.put(AbstractDateRangeSearcher.class, Panel.DATES);
        map.put(AbstractDateSearcher.class, Panel.DATES);
        map.put(AbstractRelativeDateSearcher.class, Panel.DATES);
        map.put(AssigneeSearcher.class, Panel.PEOPLE);
        map.put(DateRangeSearcher.class, Panel.DATES);
        map.put(GroupPickerSearcher.class, Panel.PEOPLE);
        map.put(ReporterSearcher.class, Panel.PEOPLE);
        map.put(UserPickerGroupSearcher.class, Panel.PEOPLE);
        map.put(UserPickerSearcher.class, Panel.PEOPLE);
        SEARCHER_PANEL_MAP = ImmutableMap.copyOf(map);
    }

    /**
     * @param issueSearcherClass The {@code IssueSearcher} class; must not be {@code null}.
     * @return The {@code Panel} to which the class belongs.
     */
    public static Panel getPanel(Class<? extends IssueSearcher> issueSearcherClass)
    {
        // Simplest case: the class was explicitly named in the map.
        if (SEARCHER_PANEL_MAP.containsKey(issueSearcherClass))
        {
            return SEARCHER_PANEL_MAP.get(issueSearcherClass);
        }

        // One of its superclasses might have been named.
        for (Map.Entry<Class<? extends IssueSearcher>, Panel> entry : SEARCHER_PANEL_MAP.entrySet())
        {
            if (entry.getKey().isAssignableFrom(issueSearcherClass))
            {
                return entry.getValue();
            }
        }

        return Panel.DETAILS;
    }
}