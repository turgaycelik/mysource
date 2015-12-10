package com.atlassian.jira.workflow;

import java.util.List;
import java.util.Set;

import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.collect.MapBuilder.build;
import static com.atlassian.jira.workflow.MockAssignableWorkflowSchemeState.EntityTable;
import static com.atlassian.jira.workflow.MockAssignableWorkflowSchemeState.SchemeTable;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v5.2
 */
public class TestOfBizAssignableWorkflowSchemeStore
{
    private MockOfBizDelegator delegator;
    private OfBizAssignableWorkflowSchemeStore store;

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

        store = new OfBizAssignableWorkflowSchemeStore(delegator);
    }

    public void testCreate()
    {
        final String defaultWofklow = "DefaultWorkflow";
        final String bugWorkflow = "BugWorkflow";
        final String bug = "bug";

        final AssignableWorkflowSchemeStore.AssignableState.Builder builder = store.builder()
                .setMappings(MapBuilder.build(null, defaultWofklow, bug, bugWorkflow))
                .setName("name").setDescription("description");

        final AssignableWorkflowSchemeStore.AssignableState beforeState = builder.build();
        final AssignableWorkflowSchemeStore.AssignableState afterState = store.create(beforeState);

        assertNotNull(afterState);
        assertNotNull(afterState.getId());
        final Set<MockAssignableWorkflowSchemeState> actual = MockAssignableWorkflowSchemeState.readSchemes(delegator);
        assertEquals(singleton(new MockAssignableWorkflowSchemeState(afterState)), actual);
    }

    @Test
    public void testDelete()
    {
        new MockAssignableWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted").saveTo(delegator);

        final MockAssignableWorkflowSchemeState keepMe = new MockAssignableWorkflowSchemeState()
                .setId(10002L).setMapping("keep", "keep").saveTo(delegator);

        assertTrue(store.delete(10000));
        assertEquals(singleton(keepMe), MockAssignableWorkflowSchemeState.readSchemes(delegator));
        assertFalse(store.delete(10000));
    }

    @Test
    public void testGet()
    {
        new MockAssignableWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted")
                .setName("name").saveTo(delegator);

        final MockAssignableWorkflowSchemeState keepMe = new MockAssignableWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep")
                .setName("name").setDescription("shadsjhkadds").saveTo(delegator);

        assertNull(store.get(1));
        final AssignableWorkflowSchemeStore.AssignableState state = store.get(10002L);
        assertEquals(keepMe, new MockAssignableWorkflowSchemeState(state));

        assertNull(store.get(102728282L));
    }

    @Test
    public void testGetAll()
    {
        final MockAssignableWorkflowSchemeState deleteMe = new MockAssignableWorkflowSchemeState()
                .setId(10000L)
                .setDefaultWorkflow("default").setDescription("description").setName("name").saveTo(delegator);

        final MockAssignableWorkflowSchemeState keepMe = new MockAssignableWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep").setMapping("wtf", "wft")
                .setName("name2")
                .saveTo(delegator);

        final Set<MockAssignableWorkflowSchemeState> allSchemes = MockAssignableWorkflowSchemeState.convert(store.getAll());
        assertEquals(Sets.newHashSet(deleteMe, keepMe), allSchemes);
    }

    @Test
    public void testRenameWorkflow()
    {
        final MockAssignableWorkflowSchemeState deleteMe = new MockAssignableWorkflowSchemeState()
                .setId(10000L)
                .setName("testRenameWorkflow")
                .setDefaultWorkflow("wtf").saveTo(delegator);

        final MockAssignableWorkflowSchemeState keepMe = new MockAssignableWorkflowSchemeState()
                .setId(10002L)
                .setName("something")
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        final String newName = "What the ....";
        assertTrue(store.renameWorkflow("wtf", newName));

        Set<MockAssignableWorkflowSchemeState> expected = Sets.newHashSet();
        expected.add(new MockAssignableWorkflowSchemeState(deleteMe).setDefaultWorkflow(newName));
        expected.add(new MockAssignableWorkflowSchemeState(keepMe).setMapping("wtf", newName));

        assertEquals(expected, MockAssignableWorkflowSchemeState.convert(store.getAll()));
    }

    @Test
    public void testUpdate()
    {
        final MockAssignableWorkflowSchemeState deleteMe = new MockAssignableWorkflowSchemeState()
                .setId(10000L).setName("ReallyGoodName")
                .setDefaultWorkflow("wtf").setDescription("ReallyGoodDescription")
                .saveTo(delegator);

        final MockAssignableWorkflowSchemeState keepMe = new MockAssignableWorkflowSchemeState()
                .setId(10002L).setName("testUpdate").setDescription(null)
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        //Remove default and add new mapping.
        AssignableWorkflowSchemeStore.AssignableState.Builder state = store.get(deleteMe.getId()).builder();
        state.setMappings(build("new", "mapping")).setName("NewName").build();

        deleteMe.setMappings(build("new", "mapping")).setName("NewName");
        MockAssignableWorkflowSchemeState returnedState = new MockAssignableWorkflowSchemeState(store.update(state.build()));
        assertEquals(deleteMe, returnedState);

        //Add default and change exisiting entity.
        state = store.get(keepMe.getId()).builder();
        state.setMappings(MapBuilder.build(null, "DefaultWorkflow", "wtf", "NewDefaultWorkflow", "keep", "keep"));
        state.setDescription("NewDescrption");
        store.update(state.build());

        keepMe.setDefaultWorkflow("DefaultWorkflow").setMapping("wtf", "NewDefaultWorkflow").setDescription("NewDescrption");
        returnedState = new MockAssignableWorkflowSchemeState(store.update(state.build()));
        assertEquals(keepMe, returnedState);

        final Set<MockAssignableWorkflowSchemeState> allSchemes = MockAssignableWorkflowSchemeState.readSchemes(delegator);
        assertEquals(Sets.newHashSet(deleteMe, keepMe), allSchemes);
    }

    @Test
    public void testBuilder()
    {
        AssignableWorkflowSchemeStore.AssignableState.Builder builder = store.builder();

        assertNull(builder.getName());
        assertNull(builder.getDescription());

        builder.setName("   name");
        builder.setDescription("description   ");

        assertEquals("name", builder.getName());
        assertEquals("description", builder.getDescription());
        assertNull(builder.setDescription("    ").getDescription());

        checkNameError(builder, "    ");
        checkNameError(builder, null);
        checkNameError(builder, StringUtils.repeat("*", 256));

        WorkflowSchemeAssertions.assertSetMapping(builder);

        AssignableWorkflowSchemeStore.AssignableState schemeState = builder.build();
        assertEquals(builder.getMappings(), schemeState.getMappings());
        assertEquals(builder.getName(), schemeState.getName());
        assertEquals(builder.getDescription(), schemeState.getDescription());
        assertEquals(builder.getId(), schemeState.getId());

    }

    private void checkNameError(AssignableWorkflowSchemeStore.AssignableState.Builder builder, String name)
    {
        String beforeName = builder.getName();
        try
        {
            builder.setName(name);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(beforeName, builder.getName());
        }
    }

    @Test
    public void testGetNoneDraftsUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");
        Iterable<AssignableWorkflowSchemeStore.AssignableState> drafts = store.getSchemesUsingWorkflow(workflow);
        assertTrue(Iterables.isEmpty(drafts));
    }

    @Test
    public void testGetTwoDraftsUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");

        MockAssignableWorkflowSchemeState schemeOne = new MockAssignableWorkflowSchemeState()
                .setId(10001L).setName("something")
                .setMapping("issuetype", workflow.getName())
                .saveTo(delegator);

        MockAssignableWorkflowSchemeState schemeTwo = new MockAssignableWorkflowSchemeState()
                .setId(10002L).setName("Else")
                .setMapping("issuetype", workflow.getName())
                .saveTo(delegator);

        Iterable<AssignableWorkflowSchemeStore.AssignableState> drafts = store.getSchemesUsingWorkflow(workflow);

        assertEquals(2, Iterables.size(drafts));
        assertEquals(Sets.newHashSet(schemeOne, schemeTwo), MockAssignableWorkflowSchemeState.readSchemes(delegator));
    }
}
