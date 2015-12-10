package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.labels.Labels;

/**
 * Used to make assertions about labels.
 *
 * @since v4.2
 */
public interface LabelAssertions
{
    /**
     * Asserts that labels exist for the given context.
     *
     * @param issueId The id of the issue
     * @param fieldId The id of the field.  Use 'labels' for system field
     */
    void assertLabelsExist(String issueId, String fieldId);

    /**
     * Asserts that labels don't exist for the given context.
     *
     * @param issueId The id of the issue
     * @param fieldId The id of the field.  Use 'labels' for system field
     */
    void assertLabelsDontExist(String issueId, String fieldId);


    /**
     * Asserts that the given labels match the labels in the given context
     *
     * @param issueId        The id of the issue
     * @param fieldId        The id of the field.  Use 'labels' for system field
     * @param expectedLabels The expected labels
     */
    void assertLabels(String issueId, String fieldId, Labels expectedLabels);

    /**
     * Asserts that the given labels match the labels in the given context
     *
     * @param issueId        The id of the issue
     * @param expectedLabels The expected labels
     */
    void assertSystemLabels(String issueId, Labels expectedLabels);

    /**
     * Asserts that the given labels are contained in the labels in the given context
     *
     * @param issueId        The id of the issue
     * @param fieldId        The id of the field.  Use 'labels' for system field
     * @param expectedLabels The expected labels
     */
    void assertLabelsContain(String issueId, String fieldId, Labels expectedLabels);
}