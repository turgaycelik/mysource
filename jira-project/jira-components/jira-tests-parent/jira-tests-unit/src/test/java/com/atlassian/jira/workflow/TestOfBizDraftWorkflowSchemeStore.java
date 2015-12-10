package com.atlassian.jira.workflow;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.collect.MapBuilder.build;
import static com.atlassian.jira.workflow.MockDraftWorkflowSchemeState.EntityTable;
import static com.atlassian.jira.workflow.MockDraftWorkflowSchemeState.SchemeTable;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.2
 */
public class TestOfBizDraftWorkflowSchemeStore
{
    private MockOfBizDelegator delegator;
    private Clock clock;
    private OfBizDraftWorkflowSchemeStore store;

    @Before
    public void setUp() throws Exception
    {
        delegator = new MockOfBizDelegator()
        {
            @Override
            public List<GenericValue> getRelated(String relationName, GenericValue gv)
            {
                if (SchemeTable.ENTITY_RELATIONSHIP.equals(relationName))
                {
                    return findByAnd(EntityTable.ENTITY,
                            FieldMap.build(EntityTable.Columns.WORKFLOW_SCHEME, gv.getLong(SchemeTable.Columns.ID)));
                }
                else
                {
                    return super.getRelated(relationName, gv);
                }
            }
        };

        clock = new ConstantClock(System.currentTimeMillis());
        store = new OfBizDraftWorkflowSchemeStore(delegator, clock);
    }

    public void testCreate()
    {
        final String defaultWofklow = "DefaultWorkflow";
        final String bugWorkflow = "BugWorkflow";
        final String bug = "bug";
        final long parentId = 10003L;
        final String lastModified = "admin";

        final DraftWorkflowSchemeStore.DraftState.Builder builder = store.builder(parentId)
                .setMappings(MapBuilder.build(null, defaultWofklow, bug, bugWorkflow))
                .setLastModifiedUser(lastModified);

        final DraftWorkflowSchemeStore.DraftState beforeState = builder.build();
        final DraftWorkflowSchemeStore.DraftState afterState = store.create(beforeState);

        assertNotNull(afterState);
        assertNotNull(afterState.getId());
        final Set<MockDraftWorkflowSchemeState> actual = MockDraftWorkflowSchemeState.readSchemes(delegator);
        assertEquals(singleton(new MockDraftWorkflowSchemeState(afterState)), actual);

        final MockDraftWorkflowSchemeState scheme = Iterables.getOnlyElement(actual);
        assertEquals(clock.getCurrentDate(), scheme.getLastModifiedDate());
    }

    @Test
    public void testDelete()
    {
        new MockDraftWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted").saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L).setMapping("keep", "keep").saveTo(delegator);

        assertTrue(store.delete(10000));
        assertEquals(singleton(keepMe), MockDraftWorkflowSchemeState.readSchemes(delegator));
        assertFalse(store.delete(10000));
    }

    @Test
    public void testDeleteByParentId()
    {
        new MockDraftWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted").setParentSchemeId(10002).saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L).setParentSchemeId(10000).setMapping("keep", "keep").saveTo(delegator);

        assertTrue(store.deleteByParentId(10002));
        assertEquals(singleton(keepMe), MockDraftWorkflowSchemeState.readSchemes(delegator));
        assertFalse(store.deleteByParentId(10002));
    }

    @Test
    public void testGet()
    {
        new MockDraftWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted")
                .setLastModifiedDate(new Date()).setLastModifiedUser("something").saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep")
                .setLastModifiedDate(new Date()).setLastModifiedUser("user").saveTo(delegator);

        assertNull(store.get(1));
        final DraftWorkflowSchemeStore.DraftState state = store.get(10002L);
        assertEquals(keepMe, new MockDraftWorkflowSchemeState(state));

        assertNull(store.get(102728282L));
    }

    @Test
    public void testGetAll()
    {
        final MockDraftWorkflowSchemeState deleteMe = new MockDraftWorkflowSchemeState()
                .setId(10000L)
                .setDefaultWorkflow("default").setLastModifiedDate(new Date()).setLastModifiedUser("user").saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep").setMapping("wtf", "wft")
                .setLastModifiedDate(new Date()).setLastModifiedUser("anotherUser")
                .saveTo(delegator);

        final Set<MockDraftWorkflowSchemeState> allSchemes = MockDraftWorkflowSchemeState.convert(store.getAll());
        assertEquals(Sets.newHashSet(deleteMe, keepMe), allSchemes);
    }

    @Test
    public void testRenameWorkflow()
    {
        final MockDraftWorkflowSchemeState deleteMe = new MockDraftWorkflowSchemeState()
                .setId(10000L)
                .setLastModifiedDate(new Date())
                .setDefaultWorkflow("wtf").saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L)
                .setLastModifiedDate(new Date())
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        final String newName = "What the ....";
        assertTrue(store.renameWorkflow("wtf", newName));

        Set<MockDraftWorkflowSchemeState> expected = Sets.newHashSet();
        expected.add(new MockDraftWorkflowSchemeState(deleteMe).setDefaultWorkflow(newName));
        expected.add(new MockDraftWorkflowSchemeState(keepMe).setMapping("wtf", newName));

        assertEquals(expected, MockDraftWorkflowSchemeState.convert(store.getAll()));
    }

    @Test
    public void testGetForParent()
    {
        final MockDraftWorkflowSchemeState deleteMe = new MockDraftWorkflowSchemeState()
                .setId(10000L).setParentSchemeId(10000L).setLastModifiedDate(new Date())
                .setDefaultWorkflow("wtf").saveTo(delegator);

        new MockDraftWorkflowSchemeState()
                .setId(10002L).setParentSchemeId(10000L)
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        // in the event that we have multiple drafts with the same parent, always return the first one (ordered by ID)
        assertEquals(deleteMe, new MockDraftWorkflowSchemeState(store.getDraftForParent(10000L)));
        assertNull(store.getDraftForParent(1));
    }

    @Test
    public void testHasDraftForParent()
    {
        new MockDraftWorkflowSchemeState()
                .setId(10000L).setParentSchemeId(10000L)
                .setDefaultWorkflow("wtf").saveTo(delegator);

        new MockDraftWorkflowSchemeState()
                .setId(10002L).setParentSchemeId(102020)
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        assertTrue(store.hasDraftForParent(10000));
        assertFalse(store.hasDraftForParent(1));
    }

    @Test
    public void testUpdate()
    {
        final Date now = new Date();
        final MockDraftWorkflowSchemeState deleteMe = new MockDraftWorkflowSchemeState()
                .setId(10000L).setParentSchemeId(10000L)
                .setDefaultWorkflow("wtf").setLastModifiedUser("someone")
                .setLastModifiedDate(now).saveTo(delegator);

        final MockDraftWorkflowSchemeState keepMe = new MockDraftWorkflowSchemeState()
                .setId(10002L).setParentSchemeId(102020)
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .setLastModifiedDate(now).setLastModifiedUser("someoneelse")
                .saveTo(delegator);

        //Remove default and add new mapping.
        DraftWorkflowSchemeStore.DraftState.Builder state = store.get(deleteMe.getId()).builder();
        state.setMappings(build("new", "mapping")).build();

        deleteMe.setMappings(build("new", "mapping"));
        MockDraftWorkflowSchemeState returnedState = new MockDraftWorkflowSchemeState(store.update(state.build()));
        deleteMe.setLastModifiedDate(clock.getCurrentDate());
        assertEquals(deleteMe, returnedState);

        //Add default and change exisiting entity.
        state = store.get(keepMe.getId()).builder();
        state.setMappings(MapBuilder.build(null, "DefaultWorkflow", "wtf", "NewDefaultWorkflow", "keep", "keep"));
        store.update(state.build());

        keepMe.setDefaultWorkflow("DefaultWorkflow").setMapping("wtf", "NewDefaultWorkflow");
        returnedState = new MockDraftWorkflowSchemeState(store.update(state.build()));
        keepMe.setLastModifiedDate(clock.getCurrentDate());
        assertEquals(keepMe, returnedState);

        final Set<MockDraftWorkflowSchemeState> allSchemes = MockDraftWorkflowSchemeState.readSchemes(delegator);
        assertEquals(Sets.newHashSet(deleteMe, keepMe), allSchemes);
    }

    @Test
    public void testBuilder()
    {
        DraftWorkflowSchemeStore.DraftState.Builder builder = store.builder(101010L);

        assertEquals(101010L, builder.getParentSchemeId());

        builder.setLastModifiedUser(null);
        assertNull(builder.getLastModifiedUser());

        //User can be anything. Its not stripped.
        builder.setLastModifiedUser(" user ");
        assertEquals(" user ", builder.getLastModifiedUser());

        builder.setLastModifiedUser(null);
        assertNull(null, builder.getLastModifiedUser());

        WorkflowSchemeAssertions.assertSetMapping(builder);

        DraftWorkflowSchemeStore.DraftState schemeState = builder.build();
        assertEquals(builder.getMappings(), schemeState.getMappings());
        assertEquals(builder.getParentSchemeId(), schemeState.getParentSchemeId());
        assertEquals(builder.getId(), schemeState.getId());

    }

    @Test
    public void testGetNoneDraftsUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");
        Iterable<DraftWorkflowSchemeStore.DraftState> drafts = store.getSchemesUsingWorkflow(workflow);
        assertTrue(Iterables.isEmpty(drafts));
    }

    @Test
    public void testGetTwoDraftsUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");

        Date date = new Date();
        MockDraftWorkflowSchemeState schemeOne = new MockDraftWorkflowSchemeState()
                .setId(10001L).setParentSchemeId(102020)
                .setMapping("issuetype", workflow.getName())
                .setLastModifiedDate(date)
                .saveTo(delegator);

        MockDraftWorkflowSchemeState schemeTwo = new MockDraftWorkflowSchemeState()
                .setId(10002L).setParentSchemeId(102020)
                .setMapping("issuetype", workflow.getName())
                .setLastModifiedDate(date)
                .saveTo(delegator);

        Iterable<DraftWorkflowSchemeStore.DraftState> drafts = store.getSchemesUsingWorkflow(workflow);

        assertEquals(2, Iterables.size(drafts));
        assertEquals(Sets.newHashSet(schemeOne, schemeTwo), MockDraftWorkflowSchemeState.readSchemes(delegator));
    }

    @Test
    public void testGetParentId()
    {
        MockDraftWorkflowSchemeState schemeOne = new MockDraftWorkflowSchemeState()
                .setId(10001L)
                .setParentSchemeId(102020)
                .saveTo(delegator);
        assertEquals(schemeOne.getParentSchemeId(), store.getParentId(schemeOne.getId()).longValue());
        assertNull(store.getParentId(12345));
    }
}
