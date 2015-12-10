/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.amendment.CreateEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationCreate;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestPrimaryEntityRelationCreate
{
    private static final String WAS_CREATED = "wasCreated";

    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private PrimaryEntityRelationCreate simpleRelationEntityCheck;

    @Before
    public void setUp() throws Exception
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());

        simpleRelationEntityCheck = new PrimaryEntityRelationCreate(ofBizDelegator, 1, "Related", "OSWorkflowEntry", "workflowId",
                ImmutableMap.of("name", "jira", "state", 0));

        // Need to create this so the Entity Check has access to the entity it is operating on.
        new EntityIntegrityCheckImpl(1, "Mock", "Issue", simpleRelationEntityCheck);
    }

    @Test
    public void testPreview() throws IntegrityException, GenericEntityException
    {
        when(ofBizDelegator.createValue(anyString(), anyMap()))
                .thenThrow(new UnsupportedOperationException("Preview should not be adding anything to the DB."));

        setUpAllIssues(
                mockIssueWithoutRelatedGv(101),
                mockIssueWithoutRelatedGv(102),
                mockIssueWithRelatedGv(103)
        );

        final List<CreateEntityAmendment> previewAmendments = simpleRelationEntityCheck.preview();
        assertAmmendmentsMentionSpecificEntityIds(previewAmendments, 101, 102);
    }

    @Test
    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // we expect that two entities will be created as correction of current data:
        final GenericValue correction1 = new MockGenericValue("correction");
        final GenericValue correction2 = new MockGenericValue("correction");

        when(ofBizDelegator.createValue(anyString(), anyMap()))
                .thenAnswer(markAsCreatedAndReturn(correction1))
                .thenAnswer(markAsCreatedAndReturn(correction2))
                .thenReturn(null);

        setUpAllIssues(
                mockIssueWithoutRelatedGv(104),
                mockIssueWithRelatedGv(105),
                mockIssueWithoutRelatedGv(106)
        );

        final List<CreateEntityAmendment> createEntityAmendments = simpleRelationEntityCheck.correct();
        assertAmmendmentsMentionSpecificEntityIds(createEntityAmendments, 104, 106);

        assertThat(correction1, wasCreated);
        assertThat(correction2, wasCreated);
    }

    private Answer<Object> markAsCreatedAndReturn(final GenericValue genericValue)
    {
        return new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                genericValue.set(WAS_CREATED, true);
                return genericValue;
            }
        };
    }

    private void setUpAllIssues(final GenericValue firstGv, final GenericValue... consecutiveGvs)
    {
        final OfBizListIterator iterator = mock(OfBizListIterator.class);
        when(iterator.next()).thenReturn(firstGv, consecutiveGvs).thenReturn(null);
        when(ofBizDelegator.findListIteratorByCondition("Issue", null)).thenReturn(iterator);
    }

    private GenericValue mockIssueWithRelatedGv(final long id) throws GenericEntityException
    {
        final GenericValue mockRelatedGv = mock(GenericValue.class);
        final GenericValue mockIssueGv = mock(GenericValue.class);
        when(mockIssueGv.getRelatedOne("RelatedOSWorkflowEntry")).thenReturn(mockRelatedGv);
        when(mockIssueGv.getLong("id")).thenReturn(id);
        return mockIssueGv;
    }

    private GenericValue mockIssueWithoutRelatedGv(final int id) throws GenericEntityException
    {
        return new MockGenericValue("issue", (long) id);
    }

    private void assertAmmendmentsMentionSpecificEntityIds(final List<CreateEntityAmendment> amendments, final Integer... ids)
    {
        final Function<CreateEntityAmendment, Integer> amendmentToEntityId = new Function<CreateEntityAmendment, Integer>()
        {
            @Override
            public Integer apply(final CreateEntityAmendment input)
            {
                return input.getEntity().getLong("id").intValue();
            }
        };

        assertThat(Iterables.transform(amendments, amendmentToEntityId), containsInAnyOrder(ids));
    }

    public static final TypeSafeMatcher<GenericValue> wasCreated = new TypeSafeMatcher<GenericValue>()
    {
        @Override
        protected boolean matchesSafely(final GenericValue genericValue)
        {
            return genericValue.getBoolean(WAS_CREATED);
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("Expected to have been created.");
        }
    };
}
