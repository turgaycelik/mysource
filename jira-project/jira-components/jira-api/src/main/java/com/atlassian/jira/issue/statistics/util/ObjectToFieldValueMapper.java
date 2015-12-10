package com.atlassian.jira.issue.statistics.util;

import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;

/**
 * Converts a given object returned from a {@link com.atlassian.jira.issue.statistics.util.StatisticsMapperWrapper}
 * result set into an appropriate form, e.g. a label for a pie chart. Determines what the type is and invokes
 * the appropriate method on a transformer for conversion
 *
 * @since v4.1
 */
public class ObjectToFieldValueMapper
{
    /**
     * Transforms the object in stats result set into an appropriate type specified by the given
     * transformer.
     *
     * @param fieldType the fieldType the statsMapperWrapper was created for
     * @param input the object to convert
     * @param url an optional url to associate with the object
     * @param transformer the transformer to do the actual conversion. The return type is determined by this object
     * @param <T> the conversion result type, e.g. a string or a StatsMarkup
     * @return the result of the conversion
     *
     **/

    static public <T> T transform(final String fieldType, final Object input, final String url, final FieldValueToDisplayTransformer<T> transformer)
    {
        final T name;
        if (input == FilterStatisticsValuesGenerator.IRRELEVANT)
        {
            name = transformer.transformFromIrrelevant(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.PROJECT.equals(fieldType))
        {
            name = transformer.transformFromProject(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.ASSIGNEES.equals(fieldType))
        {
            name = transformer.transformFromAssignee(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.REPORTER.equals(fieldType))
        {
            name = transformer.transformFromReporter(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.CREATOR.equals(fieldType))
        {
            name = transformer.transformFromCreator(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.RESOLUTION.equals(fieldType))
        {
            name = transformer.transformFromResolution(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.PRIORITIES.equals(fieldType))
        {
            name = transformer.transformFromPriority(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.ISSUETYPE.equals(fieldType))
        {
            name = transformer.transformFromIssueType(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.STATUSES.equals(fieldType))
        {
            name = transformer.transformFromStatus(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.COMPONENTS.equals(fieldType))
        {
            name = transformer.transformFromComponent(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.VERSION.equals(fieldType) || FilterStatisticsValuesGenerator.ALLVERSION.equals(fieldType))
        {
            name = transformer.transformFromVersion(fieldType, input, url);
        }
        else if (FilterStatisticsValuesGenerator.FIXFOR.equals(fieldType) || FilterStatisticsValuesGenerator.ALLFIXFOR.equals(fieldType))
        {
            name = transformer.transformFromFixFor(fieldType, input, url);
        }
        else if(FilterStatisticsValuesGenerator.LABELS.equals(fieldType))
        {
            name = transformer.transformFromLabels(fieldType, input, url);
        }
        else // must be a custom field
        {
            name = transformer.transformFromCustomField(fieldType, input, url);
        }
        return name;

    }
}
