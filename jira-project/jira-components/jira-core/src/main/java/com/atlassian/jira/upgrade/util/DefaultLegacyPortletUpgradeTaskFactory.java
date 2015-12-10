package com.atlassian.jira.upgrade.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DefaultLegacyPortletUpgradeTaskFactory implements LegacyPortletUpgradeTaskFactory
{
    private static final Logger log = Logger.getLogger(DefaultLegacyPortletUpgradeTaskFactory.class);

    private final Set<LegacyPortletUpgradeTask> upgradeTasks;
    private static final int DEFAULT_MAX_ROWS = 50;
    private final int maxRows;

    public DefaultLegacyPortletUpgradeTaskFactory(final ApplicationProperties applicationProperties)
    {
        this.upgradeTasks = getUpgradeTasks();
        maxRows = getMaxRows(applicationProperties);
    }

    private int getMaxRows(final ApplicationProperties applicationProperties)
    {
        try
        {
            if (applicationProperties != null)
            {
                final String maxRowsStr = applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows");
                if (StringUtils.isNotBlank(maxRowsStr))
                {
                    return Integer.valueOf(maxRowsStr);
                }
                else
                {
                    log.warn("'jira.table.gadget.max.rows' doesn't exist in jira-application.properties");
                }
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("'jira.table.gadget.max.rows' contains something thats not a number!", e);
        }
        return DEFAULT_MAX_ROWS;
    }

    public Map<String, LegacyPortletUpgradeTask> createPortletToUpgradeTaskMapping()
    {
        final MapBuilder<String, LegacyPortletUpgradeTask> builder = MapBuilder.newBuilder();
        for (LegacyPortletUpgradeTask upgradeTask : upgradeTasks)
        {
            builder.add(upgradeTask.getPortletKey(), upgradeTask);
        }
        //returning these in order, just in case the upgrade tasks need to run in a specific order.  Shouldn't really
        // be necessary since these upgrade tasks should be independent but just in case.
        return builder.toSortedMap();
    }

    private Set<LegacyPortletUpgradeTask> getUpgradeTasks()
    {
        CollectionBuilder<LegacyPortletUpgradeTask> legacyPortletUpgradeTaskCollectionBuilder = CollectionBuilder.<LegacyPortletUpgradeTask>newBuilder().
                add(new ProjectStatsLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:projectstats", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:stats-gadget/gadgets/stats-gadget.xml")).
                add(new ProjectStatsLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:filterstats", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:stats-gadget/gadgets/stats-gadget.xml")).
                add(new ProjectStatsLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:pie", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:pie-chart-gadget/gadgets/piechart-gadget.xml")).
                add(new RoadMapPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:roadmap", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:road-map-gadget/gadgets/roadmap-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:introduction", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:savedfilters", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:favourite-filters-gadget/gadgets/favourite-filters-gadget.xml")).
                add(new CreatedVsResolvedLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:createdvsresolved", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:created-vs-resolved-issues-chart-gadget/gadgets/createdvsresolved-gadget.xml")).
                add(new BugzillaLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:bugzilla", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:bugzilla/gadgets/bugzilla-id-search.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:admin", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml")).
                add(new ProjectLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:projects", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:project-gadget/gadgets/project-gadget.xml")).
                add(new ProjectLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:project", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:project-gadget/gadgets/project-gadget.xml")).
                add(new ProjectTableLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:projecttable", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:project-gadget/gadgets/project-gadget.xml")).
                add(new FilterResultsLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:searchrequest", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:filter-results-gadget/gadgets/filter-results-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:averageage", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:average-age-chart-gadget/gadgets/average-age-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:recentlycreated", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:recently-created-chart-gadget/gadgets/recently-created-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:userissues", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:quicklinks-gadget/gadgets/quicklinks-gadget.xml")).
                add(new AssignedToMeLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:assignedtome", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml")).
                add(new InProgressLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:inprogress", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:in-progress-gadget/gadgets/in-progress-gadget.xml")).
                add(new WatchedPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:mywatches", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:watched-gadget/gadgets/watched-gadget.xml")).
                add(new VotedPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:myvotes", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:voted-gadget/gadgets/voted-gadget.xml")).
                add(new TwoDimensionalStatsLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:filterstatsdouble", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:two-dimensional-stats-gadget/gadgets/two-dimensional-stats-gadget.xml")).
                add(new TimeSinceLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:timesince", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:time-since-chart-gadget/gadgets/timesince-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:resolutiontime", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:resolution-time-gadget/gadgets/resolution-time-gadget.xml")).
                add(new SimpleLegacyPortletUpgradeTask("com.atlassian.jira.plugin.system.portlets:text", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:text-gadget/gadgets/text-gadget.xml")).
                // fisheye plugin portlets
                add(new CrucibleChartingPortletUpgradeTask(
                        "com.atlassian.jira.ext.fisheye:cruciblecharting",
                        "rest/gadgets/1.0/g/com.atlassian.jirafisheyeplugin:crucible-charting-gadget/gadgets/crucible-charting-gadget.xml")).

                add(new FishEyeChartingPortletUpgradeTask(
                        "com.atlassian.jira.ext.fisheye:fisheyecharting",
                        "rest/gadgets/1.0/g/com.atlassian.jirafisheyeplugin:fisheye-charting-gadget/gadgets/fisheye-charting-gadget.xml")).

                add(new FishEyeRecentCommitsPortletUpgradeTask(
                        "com.atlassian.jira.ext.fisheye:fisheyerecentcommits",
                        "rest/gadgets/1.0/g/com.atlassian.jirafisheyeplugin:fisheye-recent-commits-gadget/gadgets/fisheye-recent-commits-gadget.xml")

                );
        return legacyPortletUpgradeTaskCollectionBuilder.asSet();
    }


    private static void rebindEntryIfExists(final Map<String, String> map, String oldKey, String newKey)
    {
        String oldVal = map.remove(oldKey);
        if (oldVal != null)
        {
            map.put(newKey, oldVal);
        }
    }

    private void scrubNumberToShow(Map<String, String> map, String key)
    {
        if (map.containsKey(key))
        {
            final String numToShowStr = map.get(key);
            try
            {
                if (StringUtils.isNumeric(numToShowStr) && StringUtils.isNotBlank(numToShowStr))
                {
                    int num = Integer.parseInt(numToShowStr);
                    if (num > maxRows)
                    {
                        map.put(key, "" + maxRows);
                    }
                    else if (num <= 0)
                    {
                        map.put(key, "5");
                    }
                }
                else
                {
                    map.put(key, "5");
                }
            }
            catch (NumberFormatException e)
            {
                map.put(key, "5");
            }
        }
    }

    private class FilterResultsLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public FilterResultsLegacyPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");

            scrubNumberToShow(map, "num");

            String filter = map.remove("filterid");
            if (filter != null)
            {
                if (filter.startsWith("filter-"))
                {
                    map.put("filterId", filter);
                }
                else
                {
                    map.put("filterId", "filter-" + filter);
                }
            }


            return map;
        }
    }

    private class BugzillaLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public BugzillaLegacyPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "bugzilla_url", "bugzillaUrl");

            return map;
        }
    }

    private class TimeSinceLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public TimeSinceLegacyPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "cumulative", "isCumulative");
            return map;
        }
    }

    private class TwoDimensionalStatsLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public TwoDimensionalStatsLegacyPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "xAxis", "xstattype");
            rebindEntryIfExists(map, "yAxis", "ystattype");
            rebindEntryIfExists(map, "yAxisDirection", "sortDirection");
            rebindEntryIfExists(map, "yAxisOrder", "sortBy");

            String filter = map.remove("filterid");
            if (filter != null)
            {
                if (filter.startsWith("filter-"))
                {
                    map.put("filterId", filter);
                }
                else
                {
                    map.put("filterId", "filter-" + filter);
                }
            }

            final String key = "numberToShow";

            if (map.containsKey(key))
            {
                final String numToShowStr = map.get(key);
                try
                {
                    if (StringUtils.isNumeric(numToShowStr) && StringUtils.isNotBlank(numToShowStr))
                    {
                        int num = Integer.parseInt(numToShowStr);
                        if (num <= 0)
                        {
                            map.put(key, "5");
                        }
                    }
                    else
                    {
                        map.put(key, "5");
                    }
                }
                catch (NumberFormatException e)
                {
                    map.put(key, "5");
                }
            }

            return map;
        }

    }

    private class VotedPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public VotedPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");
            rebindEntryIfExists(map, "showTotals", "showTotalVotes");

            scrubNumberToShow(map, "num");

            return map;
        }
    }

    private class WatchedPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        public WatchedPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");
            rebindEntryIfExists(map, "showTotals", "showTotalWatches");

            scrubNumberToShow(map, "num");

            return map;
        }
    }

    private class AssignedToMeLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private AssignedToMeLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");
            rebindEntryIfExists(map, "showHeader", "displayHeader");

            scrubNumberToShow(map, "num");

            return map;
        }
    }

    private class InProgressLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private InProgressLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");
            rebindEntryIfExists(map, "showHeader", "displayHeader");

            scrubNumberToShow(map, "num");


            return map;
        }
    }

    private class ProjectStatsLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private ProjectStatsLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            String project = map.remove("projectid");
            if (project != null)
            {
                if (project.startsWith("project-"))
                {
                    map.put("projectOrFilterId", project);
                }
                else
                {
                    map.put("projectOrFilterId", "project-" + project);
                }
            }
            String filter = map.remove("filterid");
            if (filter != null)
            {
                if (filter.startsWith("filter-"))
                {
                    map.put("projectOrFilterId", filter);
                }
                else
                {
                    map.put("projectOrFilterId", "filter-" + filter);
                }

            }
            rebindEntryIfExists(map, "showclosed", "includeResolvedIssues");
            rebindEntryIfExists(map, "statistictype", "statType");
            rebindEntryIfExists(map, "sortOrder", "sortBy");

            return map;
        }
    }

    private class ProjectTableLegacyPortletUpgradeTask extends ProjectLegacyPortletUpgradeTask
    {
        private ProjectTableLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        @Override
        public Map<String, String> convertUserPrefs(PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);

            rebindEntryIfExists(map, "numofcolumns", "cols");

            scrubColsToShow(map, "cols");
            map.put("viewType", "collapsed");

            return map;
        }
        private void scrubColsToShow(Map<String, String> map, String key)
        {
            if (map.containsKey(key))
            {
                final String numToShowStr = map.get(key);
                if (StringUtils.isNotBlank(numToShowStr))
                {
                    if (numToShowStr.equals("1"))
                    {
                        map.put(key, "single-col");
                    }
                    else if (numToShowStr.equals("2"))
                    {
                        map.put(key, "two-col");
                    }
                    else if (numToShowStr.equals("3"))
                    {
                        map.put(key, "three-col");
                    }
                    else
                    {
                        map.put(key, "three-col");
                    }
                }
                else
                {
                    map.put(key, "three-col");
                }
            }
        }
    }

    private class ProjectLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private ProjectLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            String project = map.remove("projectid");
            //These are mutually exclusive in the existing portlets
            if (project != null)
            {
                map.put("projectsOrCategories", project);
            }
            String projectCat = map.remove("projectcategoryid");
            if (projectCat != null)
            {
                if (projectCat.equals(""))
                {
                    map.put("projectsOrCategories", "allprojects");
                }
                else
                {
                    map.put("projectsOrCategories", "cat" + projectCat);
                }
            }

            return map;
        }
    }

    private class RoadMapPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private RoadMapPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "maxresults", "num");
            rebindEntryIfExists(map, "projectsEnt", "projectsOrCategories");
            rebindEntryIfExists(map, "projects", "projectsOrCategories");

            return map;
        }
    }

    private class CreatedVsResolvedLegacyPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {

        private CreatedVsResolvedLegacyPortletUpgradeTask(final String portletKey, final String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "cumulative", "isCumulative");
            rebindEntryIfExists(map, "versionLabels", "versionLabel");

            return map;
        }
    }

    private class SearchRequestUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {
        public SearchRequestUpgradeTask(final String s, final String s1)
        {
            super(s, s1);
        }

        public Map<String, String> convertUserPrefs(final PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "numofentries", "num");
            rebindEntryIfExists(map, "filterid", "filterId");
            return map;
        }
    }

    private class CrucibleChartingPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {
        public CrucibleChartingPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(PropertySet propertySet)
        {
            final Integer COMMENT_VOLUME = new Integer(10);
            final Integer DEFECT_CLASSIFICATION = new Integer(20);
            final Integer DEFECT_RANK = new Integer(30);
            final Integer OPEN_REVIEWS = new Integer(40);
            final Integer OPEN_REVIEW_AGE = new Integer(DEFAULT_MAX_ROWS);
            final Map<Integer, String> NAMES = Collections.unmodifiableMap(EasyMap.build(
                    OPEN_REVIEWS, "openReviews",
                    OPEN_REVIEW_AGE, "openReviewAge",
                    COMMENT_VOLUME, "commentVolume",
                    DEFECT_CLASSIFICATION, "defectClassification",
                    DEFECT_RANK, "defectRank"
            ));
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "type", "charttype");
            String charttype = map.get("charttype");
            if (charttype != null)
            {
                String newChartType = null;
                try
                {
                    newChartType = NAMES.get(Integer.parseInt(charttype));
                }
                catch (NumberFormatException nfe)
                {
                    // ignore
                }
                if (newChartType == null)
                {
                    map.put("isConfigured", "false");
                }
                else
                {
                    map.put("charttype", newChartType);
                }
            }
            return map;
        }
    }

    private class FishEyeChartingPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {
        public FishEyeChartingPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "author", "authors");
            rebindEntryIfExists(map, "extension", "extensions");
            return map;
        }
    }

    private class FishEyeRecentCommitsPortletUpgradeTask extends SimpleLegacyPortletUpgradeTask
    {
        public FishEyeRecentCommitsPortletUpgradeTask(String portletKey, String gadgetUri)
        {
            super(portletKey, gadgetUri);
        }

        public Map<String, String> convertUserPrefs(PropertySet propertySet)
        {
            Map<String, String> map = super.convertUserPrefs(propertySet);
            rebindEntryIfExists(map, "repository", "rep");
            rebindEntryIfExists(map, "numtodisplay", "numberToShow");
            return map;
        }
    }
}
