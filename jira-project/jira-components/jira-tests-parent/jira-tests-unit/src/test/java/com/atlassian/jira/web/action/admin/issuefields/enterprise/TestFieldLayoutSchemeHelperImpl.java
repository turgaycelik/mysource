package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderByImpl;

import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestFieldLayoutSchemeHelperImpl extends MockControllerTestCase
{
    private FieldLayoutManager fieldLayoutManager;
    private SearchProvider searchProvider;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        fieldLayoutManager = mockController.getMock(FieldLayoutManager.class);
        searchProvider = mockController.getMock(SearchProvider.class);
        user = new MockUser("test");
    }

    @Test
    public void testChangeFieldLayoutAssociationRequiresMessageVisiblyEquivalent() throws Exception
    {
        final FieldLayoutScheme scheme = mockController.getMock(FieldLayoutScheme.class);
        EasyMock.expect(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(1L, 2L))
                .andReturn(true);
        
        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        assertFalse(helper.doesChangingFieldLayoutAssociationRequireMessage(user, scheme, 1L, 2L));
    }

    @Test
    public void testChangeFieldLayoutAssociationRequiresMessageDifferentSchemeHasNoProjects() throws Exception
    {
        final FieldLayoutScheme scheme = mockController.getMock(FieldLayoutScheme.class);
        EasyMock.expect(scheme.getProjects())
                .andReturn(Collections.<GenericValue>emptySet());
        EasyMock.expect(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(1L, 2L))
                .andReturn(false);

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        assertFalse(helper.doesChangingFieldLayoutAssociationRequireMessage(user, scheme, 1L, 2L));
    }

    @Test
    public void testChangeFieldLayoutAssociationRequiresMessageDifferentSchemeHasProjects() throws Exception
    {
        final Collection<GenericValue> projects = CollectionBuilder.<GenericValue>newBuilder(
                createProjectGV(111L),
                createProjectGV(222L)
        ).asList();

        final FieldLayoutScheme scheme = mockController.getMock(FieldLayoutScheme.class);
        EasyMock.expect(scheme.getProjects())
                .andReturn(projects);
        EasyMock.expect(fieldLayoutManager.isFieldLayoutsVisiblyEquivalent(1L, 2L))
                .andReturn(false);

        final Clause whereClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(111L, 222L));
        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(1L);

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        assertTrue(helper.doesChangingFieldLayoutAssociationRequireMessage(user, scheme, 1L, 2L));
    }

    @Test
    public void testChangeFieldLayoutRequiresMessageNoProjects() throws Exception
    {
        final EditableFieldLayout fieldLayout = mockController.getMock(EditableFieldLayout.class);
        EasyMock.expect(fieldLayoutManager.getRelatedProjects(fieldLayout))
                .andReturn(CollectionBuilder.<GenericValue>newBuilder().asList());

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        boolean result = helper.doesChangingFieldLayoutRequireMessage(user, fieldLayout);
        assertFalse(result);
    }

    @Test
    public void testChangeFieldLayoutRequiresMessageHasProjects() throws Exception
    {
        final Collection<GenericValue> projects = CollectionBuilder.<GenericValue>newBuilder(
                createProjectGV(111L),
                createProjectGV(222L)
        ).asList();

        final EditableFieldLayout fieldLayout = mockController.getMock(EditableFieldLayout.class);
        EasyMock.expect(fieldLayoutManager.getRelatedProjects(fieldLayout))
                .andReturn(projects);

        final Clause whereClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(111L, 222L));
        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(1L);

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        assertTrue(helper.doesChangingFieldLayoutRequireMessage(user, fieldLayout));
    }

    @Test
    public void testChangeFieldLayoutSchemeRequiresMessageVisiblyEquivalent() throws Exception
    {
        final FieldLayoutScheme scheme1 = EasyMock.createMock(FieldLayoutScheme.class);
        final FieldLayoutScheme scheme2 = EasyMock.createMock(FieldLayoutScheme.class);

        EasyMock.expect(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(10001L, 10002L))
                .andReturn(true);

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);

        assertFalse(helper.doesChangingFieldLayoutSchemeForProjectRequireMessage(user, 1L, 10001L, 10002L));
    }

    @Test
    public void testChangeFieldLayoutSchemeRequiresMessageDifferent() throws Exception
    {
        final long projectId = 1L;

        EasyMock.expect(fieldLayoutManager.isFieldLayoutSchemesVisiblyEquivalent(10001L, 10002L))
                .andReturn(false);

        final Clause whereClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(projectId));
        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(0L);

        replay();

        FieldLayoutSchemeHelperImpl helper = new FieldLayoutSchemeHelperImpl(fieldLayoutManager, searchProvider);
        
        assertFalse(helper.doesChangingFieldLayoutSchemeForProjectRequireMessage(user, projectId, 10001L, 10002L));
    }

    private MockGenericValue createProjectGV(final long projectId)
    {
        return new MockGenericValue("project", ImmutableMap.of("id", projectId));
    }
}
