/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestBulkEditBeanImpl
{
    private Project project1;
    private Project project2;

    private IssueType issueType1;
    private IssueType issueType2;

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Before
    public void dataSetUp() throws Exception
    {

        project1 = new MockProject(500, "ABC", "testProject1");
        project2 = new MockProject(501, "XYZ", "testProject2");

        issueType1 = new MockIssueType("100", "testtype");
        issueType2 = new MockIssueType("200", "another testtype");

        final MutableIssue issue1 = new MockIssue(100, "ABC-1");
        issue1.setProjectObject(project1);
        issue1.setIssueTypeObject(issueType1);
        final MutableIssue issue2 = new MockIssue(101, "ABC-2");
        issue2.setProjectObject(project1);
        issue2.setIssueTypeObject(issueType1);

        final MutableIssue issue3 = new MockIssue(102, "XYZ-1");
        issue3.setProjectObject(project2);
        issue3.setIssueTypeObject(issueType2);

        when(issueManager.getIssueObject(issue1.getId())).thenReturn(issue1);
        when(issueManager.getIssueObject(issue2.getId())).thenReturn(issue2);
        when(issueManager.getIssueObject(issue3.getId())).thenReturn(issue3);
    }

    private BulkEditBean setupBulkEditBeanWithSimilarIssues() throws GenericEntityException
    {
        final Map<String, Boolean> params = ImmutableMap.<String, Boolean>builder()
                .put(BulkEditBean.BULKEDIT_PREFIX + "100", Boolean.TRUE)
                .put(BulkEditBean.BULKEDIT_PREFIX + "101", Boolean.TRUE)
                .build();

        return setupBulkEditBean(params);
    }

    private BulkEditBean setupBulkEditBeanWithVaryingIssues() throws GenericEntityException
    {
        final Map<String, Boolean> params = ImmutableMap.<String, Boolean>builder()
                .put(BulkEditBean.BULKEDIT_PREFIX + "100", Boolean.TRUE)
                .put(BulkEditBean.BULKEDIT_PREFIX + "101", Boolean.TRUE)
                .put(BulkEditBean.BULKEDIT_PREFIX + "102", Boolean.TRUE)
                .build();

        return setupBulkEditBean(params);
    }

    private BulkEditBean setupBulkEditBean(final Map<String, Boolean> explicitParams) throws GenericEntityException
    {
        final BulkEditBean be = new BulkEditBeanImpl(ComponentAccessor.getIssueManager());
        be.setParams(explicitParams);
        be.setIssuesInUse(be.getSelectedIssues());
        return be;
    }

    @Test
    public void shouldDetectSelectedIssuesAreFromCommonProject() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithSimilarIssues(); // 2 issues from one project selected.
        assertFalse("The given issues are from one common project.", be.isMultipleProjects());
    }

    @Test
    public void shouldDetectSelectedIssuesAreFromMultipleProjects() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithVaryingIssues();
        assertTrue("The given issues are from two distinct projects.", be.isMultipleProjects());
    }

    @Test
    public void shouldExtractSingleProjectIdFromMultipleIssues() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithSimilarIssues();
        assertThat("The given issues are from one common project.", be.getProjectIds(), Matchers.containsInAnyOrder(500L));
    }

    @Test
    public void shouldExtractAllProjectIdsFromMultipleIssues() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithVaryingIssues();
        assertThat("The given issues are two distinct projects.", be.getProjectIds(), Matchers.containsInAnyOrder(500L, 501L));
    }

    @Test
    public void shouldExtractProjectObjectFromGivenIssues() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithSimilarIssues();
        assertThat("The given issues are from one common project.", be.getProjectObjects(), Matchers.containsInAnyOrder(project1));
    }

    @Test
    public void shouldExtractProjectObjectsFromIssuesFromDifferentProjects() throws Exception
    {
        final BulkEditBean be = setupBulkEditBeanWithVaryingIssues();
        assertThat("The given issues are two distinct projects.", be.getProjectObjects(), Matchers.containsInAnyOrder(project1, project2));
    }

    @Test
    public void shouldExtractIssueTypesFromMultipleIssues() throws GenericEntityException
    {
        final BulkEditBean be = setupBulkEditBeanWithVaryingIssues();
        assertThat("The given issues are of two distinct types.", be.getIssueTypes(), Matchers.containsInAnyOrder(issueType1.getId(), issueType2.getId()));
    }
}
