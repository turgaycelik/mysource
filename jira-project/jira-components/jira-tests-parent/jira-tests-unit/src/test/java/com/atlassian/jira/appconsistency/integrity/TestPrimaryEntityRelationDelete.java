/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.List;

import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationDelete;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class TestPrimaryEntityRelationDelete
{
    public static final String ENTITY_NAME = "MockEntity";
    @Mock
    private GenericDelegator genericDelegator;
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private I18nHelper i18nHelper;
    @Rule
    public TestRule initMock = MockitoMocksInContainer.forTest(this);

    private PrimaryEntityRelationDelete primaryEntityRelationDelete;
    private MockGenericValue secondIssueGV;
    private MockGenericValue thirdIssueGV;

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        final MockGenericValue projectGV = new MockGenericValue("Project", ImmutableMap.of("id", 123));

        // Create three issue or which two do no have a project.
        final MockGenericValue firstIssueGV = new MockGenericValue(ENTITY_NAME, ImmutableMap.of("id", 1000, "project", 123, "key", "ABC-1"));
        secondIssueGV = new MockGenericValue(ENTITY_NAME, ImmutableMap.of("id", 1001l, "project", -1l, "key", "ABC-2"));
        thirdIssueGV = new MockGenericValue(ENTITY_NAME, ImmutableMap.of("id", 1002l, "project", -1l, "key", "ABC-3"));
        Mockito.when(ofBizDelegator.findListIteratorByCondition(ENTITY_NAME, null)).thenReturn(
                new MockOfBizListIterator(ImmutableList.<GenericValue>of(
                        firstIssueGV,
                        secondIssueGV,
                        thirdIssueGV
                ))
        );
        primaryEntityRelationDelete = new PrimaryEntityRelationDelete(ofBizDelegator, 1, "Parent", "Project");

        // Need to create this so the Entity Check has access to the entity it is operating on.
        new EntityIntegrityCheckImpl(1, "Mock", ENTITY_NAME, primaryEntityRelationDelete);
        firstIssueGV.setRelated("ParentProject", ImmutableList.of(projectGV));

    }

    @Test
    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List<DeleteEntityAmendment> amendments = primaryEntityRelationDelete.preview();
        //noinspection unchecked
        Matcher<Iterable<DeleteEntityAmendment>> contains = Matchers.contains(ammendmentWithIdAndType(1001l, false), ammendmentWithIdAndType(1002l, false));
        Assert.assertThat(amendments, contains);

        // The preview method should not modify anything so we should have 3 issues in the database
        Mockito.verify(ofBizDelegator).findListIteratorByCondition(ENTITY_NAME, null);
        //this check is a little harsh but we want to be sure that nothing was deleted
        Mockito.verifyNoMoreInteractions(ofBizDelegator);
    }

    @Test
    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by removing the issues with no project
        List<DeleteEntityAmendment> amendments = primaryEntityRelationDelete.correct();
        //noinspection unchecked
        Matcher<Iterable<DeleteEntityAmendment>> contains = Matchers.contains(ammendmentWithIdAndType(1001l, true), ammendmentWithIdAndType(1002l, true));
        Assert.assertThat(amendments, contains);
        Mockito.verify(ofBizDelegator).removeAll(
                ImmutableList.<GenericValue>of(secondIssueGV, thirdIssueGV)
        );

    }

    private Matcher<DeleteEntityAmendment> ammendmentWithIdAndType(final long id, final boolean correction)
    {
        return new TypeSafeMatcher<DeleteEntityAmendment>()
        {
            @Override
            protected boolean matchesSafely(DeleteEntityAmendment deleteEntityAmendment)
            {
                return deleteEntityAmendment.getEntity().getLong("id") == id
                        && (correction ? deleteEntityAmendment.isCorrection() : deleteEntityAmendment.isError());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("id = " + id + " type=" + (correction ? "CORRECTIO" : "ERROR"));
            }
        };
    }
}
