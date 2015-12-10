package com.atlassian.jira.issue.statistics.util;

/**
 * Transforms a given object returned from {@link com.atlassian.jira.issue.statistics.util.StatisticsMapperWrapper}
 * result set to a more friendly form such as a string or StatsMarkup. The decision of which transformation
 * to invoke is done by {@link com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper#transform(String, Object, String, FieldValueToDisplayTransformer)}
 * The transform methods should HTML-encode unsafe values before returning them.
 *
 * @since v4.1
 */
public interface FieldValueToDisplayTransformer<T>
{
    T transformFromIrrelevant(final String fieldType, final Object input, final String url);

    T transformFromProject(final String fieldType, final Object input, final String url);

    T transformFromAssignee(final String fieldType, final Object input, final String url);

    T transformFromReporter(final String fieldType, final Object input, final String url);

    T transformFromResolution(final String fieldType, final Object input, final String url);

    T transformFromPriority(final String fieldType, final Object input, final String url);

    T transformFromIssueType(final String fieldType, final Object input, final String url);

    T transformFromStatus(final String fieldType, final Object input, final String url);

    T transformFromComponent(final String fieldType, final Object input, final String url);

    T transformFromVersion(final String fieldType, final Object input, final String url);

    T transformFromFixFor(final String fieldType, final Object input, final String url);

    T transformFromLabels(final String fieldType, final Object input, final String url);

    T transformFromCustomField(final String fieldType, final Object input, final String url);

    T transformFromCreator(final String fieldType, final Object input, final String url);
}
