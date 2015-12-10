package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

/**
 * Implementation for Label Assertions
 *
 * @since v4.2
 */
public class LabelAssertionsImpl extends AbstractFuncTestUtil implements LabelAssertions
{
    private static final String FIELDID_LABELS = "labels";

    public LabelAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    private void assertLabelsExist(Labels labels, String issueId, String fieldId)
    {
        Assert.assertTrue("Labels should exist but can't find labels for issue - " + issueId + ", field - " + fieldId, labels != null);
    }

    public void assertLabelsExist(String issueId, String fieldId)
    {
        Labels labels = Labels.parseLabels(tester, issueId, fieldId);
        assertLabelsExist(labels, issueId, fieldId);
    }

    public void assertLabelsDontExist(String issueId, String fieldId)
    {
        Labels labels = Labels.parseLabels(tester, issueId, fieldId);
        Assert.assertTrue("Labels should not exist but found labels for issue - " + issueId + ", field - " + fieldId, labels == null);
    }

    public void assertLabelsEmpty(Labels labels)
    {
        Assert.assertTrue("Labels should be empty but contains: " + labels.toString(), labels.getLabelValues().isEmpty());
    }

    public void assertLabels(String issueId, String fieldId, Labels expectedLabels)
    {
        Labels labels = Labels.parseLabels(tester, issueId, fieldId);
        assertLabelsExist(labels, issueId, fieldId);
        Assert.assertEquals(expectedLabels, labels);
    }

    public void assertSystemLabels(final String issueId, final Labels expectedLabels)
    {
        assertLabels(issueId, FIELDID_LABELS, expectedLabels);
    }

    public void assertLabelsContain(String issueId, String fieldId, Labels expectedLabels)
    {
        Labels labels = Labels.parseLabels(tester, issueId, fieldId);
        assertLabelsExist(labels, issueId, fieldId);
        Assert.assertEquals(expectedLabels.isEditable(), labels.isEditable());
        Assert.assertEquals(expectedLabels.isLinked(), labels.isLinked());
        Assert.assertEquals(expectedLabels.isLozenges(), labels.isLozenges());
        Assert.assertTrue(labels.getLabelValues().containsAll(expectedLabels.getLabelValues()));
    }

}
