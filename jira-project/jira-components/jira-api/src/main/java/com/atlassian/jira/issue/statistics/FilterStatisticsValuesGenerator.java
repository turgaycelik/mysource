package com.atlassian.jira.issue.statistics;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FilterStatisticsValuesGenerator implements ValuesGenerator
{
    protected static final Map<String, String> systemValues;

    public static final String COMPONENTS = "components";
    public static final String FIXFOR = "fixfor";
    public static final String ALLFIXFOR = "allFixfor";
    public static final String VERSION = "version";
    public static final String ALLVERSION = "allVersion";
    public static final String ASSIGNEES = "assignees";
    public static final String ISSUETYPE = "issuetype";
    public static final String PRIORITIES = "priorities";
    public static final String PROJECT = "project";
    public static final String REPORTER = "reporter";
    public static final String RESOLUTION = "resolution";
    public static final String STATUSES = "statuses";
    public static final String LABELS = "labels";
    public static final String CREATOR = "creator";
    public static final Object IRRELEVANT = new Object();

    CustomFieldManager customFieldManager = (CustomFieldManager) ComponentAccessor.getComponent(CustomFieldManager.class);

    static
    {
        Map<String, String> systemValuesTmp = new ListOrderedMap();
        systemValuesTmp.put(ASSIGNEES, "gadget.filterstats.field.statistictype.assignees");
        systemValuesTmp.put(COMPONENTS, "gadget.filterstats.field.statistictype.components");
        systemValuesTmp.put(ISSUETYPE, "gadget.filterstats.field.statistictype.issuetype");
        systemValuesTmp.put(FIXFOR, "gadget.filterstats.field.statistictype.fixfor");
        systemValuesTmp.put(ALLFIXFOR, "gadget.filterstats.field.statistictype.allfixfor");
        systemValuesTmp.put(PRIORITIES, "gadget.filterstats.field.statistictype.priorities");
        systemValuesTmp.put(PROJECT, "gadget.filterstats.field.statistictype.project");
        systemValuesTmp.put(VERSION, "gadget.filterstats.field.statistictype.version");
        systemValuesTmp.put(ALLVERSION, "gadget.filterstats.field.statistictype.allversion");
        systemValuesTmp.put(REPORTER, "gadget.filterstats.field.statistictype.reporter");
        systemValuesTmp.put(RESOLUTION, "gadget.filterstats.field.statistictype.resolution");
        systemValuesTmp.put(STATUSES, "gadget.filterstats.field.statistictype.statuses");
        systemValuesTmp.put(LABELS, "gadget.filterstats.field.statistictype.labels");
        systemValuesTmp.put(CREATOR, "gadget.filterstats.field.statistictype.creator");
        systemValues = Collections.unmodifiableMap(systemValuesTmp);
    }

    public Map getValues(Map params)
    {
        Map allValues = new ListOrderedMap();

        allValues.putAll(systemValues);
        final List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects();

        for (final CustomField customField : customFieldObjects)
        {
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                allValues.put(customField.getId(), customField.getName());
            }
        }
        return allValues;
    }

    public StatisticsMapper getStatsMapper(String statsMapperKey)
    {
        StatisticsMapper systemMapper = getSystemMapper(statsMapperKey);
        if (systemMapper != null)
            return systemMapper;

        CustomField customField = customFieldManager.getCustomFieldObject(statsMapperKey);
        if (customField == null) throw new RuntimeException("No custom field with id "+statsMapperKey);
        if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
        {
            final CustomFieldStattable customFieldStattable = (CustomFieldStattable) customField.getCustomFieldSearcher();
            return customFieldStattable.getStatisticsMapper(customField);
        }
        else
        {
            return null;
        }
    }

    /**
     * @todo Remove this ugly hack - Spring/Pico-ify! (At least the hack is localised to this one method) :)
     */
    private StatisticsMapper getSystemMapper(String statsMapperKey)
    {
        if (COMPONENTS.equals(statsMapperKey))
            return new ComponentStatisticsMapper();
        else if (ASSIGNEES.equals(statsMapperKey))
            return new AssigneeStatisticsMapper(ComponentAccessor.getUserManager(), ComponentAccessor.getJiraAuthenticationContext());
        else if (ISSUETYPE.equals(statsMapperKey))
            return new IssueTypeStatisticsMapper(ComponentAccessor.getConstantsManager());
        else if (FIXFOR.equals(statsMapperKey))
            return new FixForVersionStatisticsMapper(ComponentAccessor.getVersionManager(), false);
        else if (ALLFIXFOR.equals(statsMapperKey))
            return new FixForVersionStatisticsMapper(ComponentAccessor.getVersionManager(), true);
        else if (PRIORITIES.equals(statsMapperKey))
            return new PriorityStatisticsMapper(ComponentAccessor.getConstantsManager());
        else if (PROJECT.equals(statsMapperKey))
            return new ProjectStatisticsMapper(ComponentAccessor.getProjectManager());
        else if (VERSION.equals(statsMapperKey))
            return new RaisedInVersionStatisticsMapper(ComponentAccessor.getVersionManager(), false);
        else if (ALLVERSION.equals(statsMapperKey))
            return new RaisedInVersionStatisticsMapper(ComponentAccessor.getVersionManager(), true);
        else if (REPORTER.equals(statsMapperKey))
            return new ReporterStatisticsMapper(ComponentAccessor.getUserManager(), ComponentAccessor.getJiraAuthenticationContext());
        else if (CREATOR.equals(statsMapperKey))
            return new CreatorStatisticsMapper(ComponentAccessor.getUserManager(), ComponentAccessor.getJiraAuthenticationContext());
        else if (RESOLUTION.equals(statsMapperKey))
            return new ResolutionStatisticsMapper(ComponentAccessor.getConstantsManager());
        else if (STATUSES.equals(statsMapperKey))
            return new StatusStatisticsMapper(ComponentAccessor.getConstantsManager());
        else if(LABELS.equals(statsMapperKey))
            return new LabelsStatisticsMapper(false);

        return null; // custom field maybe?
    }
}
