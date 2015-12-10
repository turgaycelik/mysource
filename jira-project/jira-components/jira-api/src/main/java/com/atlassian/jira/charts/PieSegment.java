package com.atlassian.jira.charts;

/**
 * Describes operations for pie chart segments
 *
 * @see PieSegmentWrapper
 * @since v5.2
 */
public interface PieSegment extends Comparable
{
    String getName();

    int compareTo(Object o);

    Object getKey();

    boolean isGenerateUrl();
}